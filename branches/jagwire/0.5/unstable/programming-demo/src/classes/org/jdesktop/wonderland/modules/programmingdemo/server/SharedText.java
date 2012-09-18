/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.programmingdemo.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A text component designed to work with multiple users editing at the same
 * time.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SharedText implements Serializable {
    private static final Logger logger =
            Logger.getLogger(SharedText.class.getName());

    private static final int DEFAULT_LOOKBACK = 512;

    // the current text
    private final StringBuffer text;

    // the current version of the text
    private long version;

    // the number of transforms to store to account for out-of-order requests
    // from clients
    private final int lookback;

    // a list of transforms from previous versions
    private final List<ClientTransform> transforms;

    /**
     * Create a new shared text object
     */
    public SharedText() {
        this (null);
    }

    /**
     * Create a new shared text object with the given initial text
     * @param initialText the initial text for the buffer, or null for
     * an empty text object
     */
    public SharedText(String initialText) {
        this (initialText, DEFAULT_LOOKBACK);
    }

    /**
     * Create a new shared text object with the given initial text and
     * lookback size
     * @param initialText the initial text for the buffer, or null for
     * an empty text object
     * @param lookback the number of operations to save. Attempts to make
     * a change with an older version will cause an exception
     */
    public SharedText(String initialText, int lookback) {
        if (initialText == null) {
            initialText = "";
        }

        this.lookback = lookback;
        this.text = new StringBuffer(initialText);
        this.transforms = new ArrayList<ClientTransform>(lookback);
    }

    /**
     * Get the current text
     * @return the current text
     */
    public String getText() {
        return text.toString();
    }

    /**
     * Get the current version number
     * @return the current version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Apply the given transform. The text will be modified with the given
     * changes, updated to reflect the current state of the text relative
     * to when the change was submitted.
     *
     * The caller must supply a version number, which is the version of the
     * text they saw when they requested the change. This may be older than
     * the current version by up to <i>lookback</i> revisions. Attempts to
     * submit changes against older version will result in an exception.
     *
     * The return value is an updated transform that can be applied to the
     * previous version to get to the current version.
     *
     * @param clientID the id of the client performing this transform
     * @param version the version of the text that the transform was originally
     * generated for
     * @param transform the actual transform
     * @return the transform updated so it is appropriate to apply to the
     * previous revision in order to create the current revision
     * @throws OldRevisionException if the version is more than <i>lookback</i>
     * versions older than the current version
     */
    public Transform apply(WonderlandClientID clientID,
                           long version, Transform transform)
        throws OldRevisionException
    {
        // generate a list of transforms between the submitted version and
        // the current version
        int versionCount = (int) (getVersion() - version);
        if (versionCount > transforms.size()) {
            throw new OldRevisionException("Attempting to apply change to" +
                    " version: " + version + ", current version: " +
                    getVersion());
        }

        logger.warning("[SharedText] Apply " + transform + " @ " + version +
                       " to current version " + getVersion());

        // quick check -- if versionCount is zero, the change was made
        // against the current version, so we can just go ahead and apply
        // it without checking previous transforms
        if (versionCount > 0) {
            // create a sublist of all revisions between the current version
            // and the version this change was submitted against (versionCount).
            // Note that this starts at version + 1, since the submitter
            // already saw version.
            int end = transforms.size();
            int start = end - versionCount;
            List<ClientTransform> updates = transforms.subList(start, end);

            // now update the current transform by calling update on each of the
            // previous updates.
            for (ClientTransform ct : updates) {
                if (ct.clientID.equals(clientID)) {
                    // don't apply changes from the same client. If a client
                    // made a previous change, it will already have taken that
                    // change into account in the update it sends
                    continue;
                }

                // modify the transform
                transform = ct.transform.update(transform);
            }
        }

        logger.warning("[SharedText] After update transform " + transform + "\n" +
                       "Apply to text:\n" + text.toString());

        // go ahead and apply the transform
        transform.apply(text);

        logger.warning("[SharedText] After apply text:\n" + text.toString());

        // bump up the version number
        this.version++;

        // add the transform to the updates list, making sure to clear the
        // oldest entry if the list is too big
        ClientTransform ct = new ClientTransform();
        ct.clientID = clientID;
        ct.transform = transform;
        while (transforms.size() >= lookback) {
            transforms.remove(0);
        }
        transforms.add(ct);

        // return the updated transform
        return transform;
    }

    /**
     * Represents a change that can be made to the text, such as an
     * insertion or a deletion
     */
    public interface Transform {
        /**
         * Apply this transform to the given StringBuffer, representing the
         * current text
         * @param text the buffer to apply this change to
         */
        public void apply(StringBuffer text);

        /**
         * Update the given transform to make sure it can be applied
         * after this transform.
         * @param transform the transform to update
         * @return the updated transform
         */
        public Transform update(Transform transform);
    }

    /**
     * A transform that adds text
     */
    public static class AddTransform implements Transform, Serializable {
        private final int insertionPoint;
        private final String text;

        public AddTransform(int insertionPoint, String text) {
            this.insertionPoint = insertionPoint;
            this.text = text;
        }

        public int getInsertionPoint() {
            return insertionPoint;
        }

        public String getText() {
            return text;
        }

        public void apply(StringBuffer textBuffer) {
            textBuffer.insert(getInsertionPoint(), getText());
        }

        public Transform update(Transform transform) {
            if (transform instanceof AddTransform) {
                return updateAdd((AddTransform) transform);
            } else if (transform instanceof DeleteTransform) {
                return updateDelete((DeleteTransform) transform);
            } else if (transform instanceof MultiTransform) {
                return updateMulti((MultiTransform) transform);
            } else {
                throw new IllegalArgumentException("Unexpected type: " +
                        transform.getClass().getName());
            }
        }

        private Transform updateAdd(AddTransform add) {
            if (add.getInsertionPoint() > getInsertionPoint()) {
                // if the new text was added after the text we added,
                // move the insertion point up to account for the text
                // we added
                return new AddTransform(add.getInsertionPoint() +
                                        getText().length(), add.getText());
            } else {
                // the new text was added before the text we added, so no
                // change is needed
                return add;
            }
        }

        private Transform updateDelete(DeleteTransform delete) {
            if (delete.getDeletionPoint() >= getInsertionPoint()) {
                // if the deletion happens completely after the add,
                // adjust the deletion point to account for the text
                // we added
                return new DeleteTransform(delete.getDeletionPoint() +
                                           getText().length(), delete.getLength());
            } else if (delete.getDeletionPoint() + delete.getLength() > getInsertionPoint()) {
                // this is the tricky case -- if the deletion starts before our
                // addition, but also continues after it, we need to split the
                // delete into two parts: the unadjusted part before the
                // insert, and the adjusted part after
                int beforeLen = getInsertionPoint() - delete.getDeletionPoint();
                DeleteTransform before = new DeleteTransform(delete.getDeletionPoint(),
                                                             beforeLen);
                int afterLen = delete.getLength() - beforeLen;
                Transform after = new DeleteTransform(getInsertionPoint() +
                        getText().length(), afterLen);
                after = before.update(after);
                return new MultiTransform(before, after);
            } else {
                // the deletion is entirely before the insert, so no change
                // is needed
                return delete;
            }
        }

        private Transform updateMulti(MultiTransform multi) {
            Transform[] out = new Transform[multi.getTransforms().length];
            for (int i = 0; i < multi.getTransforms().length; i++) {
                out[i] = update(multi.getTransforms()[i]);
            }
            return new MultiTransform(out);
        }

        @Override
        public String toString() {
            return "{ADD " + getInsertionPoint() + ": " + getText() + "}";
        }
    }

    public static class DeleteTransform implements Transform, Serializable {
        private final int deletionPoint;
        private final int length;

        public DeleteTransform(int deletionPoint, int length) {
            this.deletionPoint = deletionPoint;
            this.length = length;
        }

        public int getDeletionPoint() {
            return deletionPoint;
        }

        public int getLength() {
            return length;
        }

        public void apply(StringBuffer textBuffer) {
            textBuffer.delete(getDeletionPoint(), getDeletionPoint() + getLength());
        }

        public Transform update(Transform transform) {
            if (transform instanceof AddTransform) {
                return updateAdd((AddTransform) transform);
            } else if (transform instanceof DeleteTransform) {
                return updateDelete((DeleteTransform) transform);
            } else if (transform instanceof MultiTransform) {
                return updateMulti((MultiTransform) transform);
            } else {
                throw new IllegalArgumentException("Unexpected type: " +
                        transform.getClass().getName());
            }
        }

        private Transform updateAdd(AddTransform add) {
            if (add.getInsertionPoint() >= getDeletionPoint()) {
                // if the addition is after the deletion point, shift the
                // addition point back by the amount we deleted. The only
                // trick is that if the add is in the middle of our
                // delete, we only shift it back to the beginning of the
                // delete
                int deleteAmount = getLength();
                int deleteEnd = getDeletionPoint() + getLength();
                if (deleteEnd > add.getInsertionPoint()) {
                    // shift the delete back to the start of the delete
                    deleteAmount = add.getInsertionPoint() - getDeletionPoint();
                }
                return new AddTransform(add.getInsertionPoint() - deleteAmount,
                                        add.getText());
            } else {
                // if the insertion is entirely before this deletion, no change
                // is needed
                return add;
            }
        }

        private Transform updateDelete(DeleteTransform delete) {
            if (delete.getDeletionPoint() >= getDeletionPoint() + getLength()) {
                // if the deletion is entirely after our deletion, we can
                // just shift it back by the amount we deleted
                return new DeleteTransform(delete.getDeletionPoint() - getLength(),
                                           delete.getLength());
            } else if (delete.getDeletionPoint() >= getDeletionPoint()) {
                // this deletion starts in the middle of the existing deletion,
                // so we need to shorten it to take out the overlapping portion
                int overlap = getDeletionPoint() + getLength() - delete.getDeletionPoint();
                if (overlap > delete.getLength()) {
                    // if the overlap is greater than the length, the existing
                    // deletion covers all the characters in the new deletion,
                    // so the new deletion becomes a noop
                    overlap = delete.getLength();
                }

                return new DeleteTransform(getDeletionPoint(),
                                           delete.getLength() - overlap);
            } else if (delete.getDeletionPoint() + delete.getLength() >= getDeletionPoint()) {
                // this deletion starts before our deletion, but ends in the
                // middle or after it. Again, we need to shorten the length
                // of deletion to account for the overlap
                int overlap = delete.getDeletionPoint() + delete.getLength() - getDeletionPoint();
                if (overlap > getLength()) {
                    overlap = getLength();
                }
                return new DeleteTransform(delete.getDeletionPoint(),
                                           delete.getLength() - overlap);
            } else {
                // if the deletion is entirely before our deletion, we can
                // just return it
                return delete;
            }
        }

        private Transform updateMulti(MultiTransform multi) {
            Transform[] out = new Transform[multi.getTransforms().length];
            for (int i = 0; i < multi.getTransforms().length; i++) {
                out[i] = update(multi.getTransforms()[i]);
            }
            return new MultiTransform(out);
        }

        @Override
        public String toString() {
            return "{DELETE " + getDeletionPoint() + ": " + getLength() + "}";
        }
    }

    public static class MultiTransform implements Transform, Serializable {
        Transform[] transforms;

        public MultiTransform(Transform... transforms) {
            this.transforms = transforms;
        }

        public Transform[] getTransforms() {
            return transforms;
        }

        public void apply(StringBuffer text) {
            for (Transform t : transforms) {
                t.apply(text);
            }
        }

        public Transform update(Transform transform) {
            for (Transform t : transforms) {
                transform = t.update(transform);
            }

            return transform;
        }

        @Override
        public String toString() {
            String out = "{MULTI: ";
            for (Transform t : transforms) {
                out += t.toString();
            }
            out += "}";

            return out;
        }
    }

    private static class ClientTransform implements Serializable {
        Transform transform;
        WonderlandClientID clientID;
    }

    /**
     * An exception from trying to request an update with too old a revision
     */
    public class OldRevisionException extends Exception {
        public OldRevisionException() {
            super ();
        }

        public OldRevisionException(String message) {
            super (message);
        }
    }
}
