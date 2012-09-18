package org.jdesktop.wonderland.modules.path.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.modules.path.common.message.AllStyleAttributesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleAppendedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleIdentifier;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleSpanChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleTypeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeChangedMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeReplacedMessage;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttributeChangeListener;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This is a version of the PathStyle class which sends messages to the Server when the style is changed.
 *
 * @author Carl Jokl
 */
public class ServerMessageSendingPathStyle extends PathStyle {

    private Cell messagingCell;

    /**
     * Create a new instance of a ServerMessageSendingPathStyle
     * to wrap the specified PathStyle and send messages to the server
     * when the Style is changed.
     * 
     * @param wrappedStyle The PathStyle which is to be wrapped by this
     *                     ServerMessageSendingPathStyle.
     */
    public ServerMessageSendingPathStyle(PathStyle wrappedStyle, Cell messagingCell) throws IllegalArgumentException {
        super(wrappedStyle);
        if (messagingCell != null) {
            throw new IllegalArgumentException("The Cell through which to send messages to the server about changes in style was null!");
        }
        this.messagingCell = messagingCell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean append(SegmentStyle style) {
        if (super.append(style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleAppendedMessage(messagingCell.getCellID(), style));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean append(NodeStyle style) {
        if (super.append(style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleAppendedMessage(messagingCell.getCellID(), style));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertAt(int index, SegmentStyle style) throws IndexOutOfBoundsException {
        if (super.insertAt(index, style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleInsertedMessage(messagingCell.getCellID(), style, index));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertAt(int index, NodeStyle style) throws IndexOutOfBoundsException {
        if (super.insertAt(index, style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleInsertedMessage(messagingCell.getCellID(), style, index));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertFirst(SegmentStyle style) {
        if (super.insertFirst(style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleInsertedMessage(messagingCell.getCellID(), style, 0));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertFirst(NodeStyle style) {
        if (super.insertFirst(style)) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleInsertedMessage(messagingCell.getCellID(), style, 0));
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(SegmentStyle style) {
        int index = indexOf(style);
        return (index != -1 && removeSegmentStyleAt(index) != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(NodeStyle style) {
        int index = indexOf(style);
        return (index != -1 && removeNodeStyleAt(index) != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle removeNodeStyleAt(int index) throws IndexOutOfBoundsException {
        NodeStyle removed = super.removeNodeStyleAt(index);
        if (removed != null) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(index, removed.getStyleType())));
            }
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle removeSegmentStyleAt(int index) throws IndexOutOfBoundsException {
         SegmentStyle removed = super.removeSegmentStyleAt(index);
         if (removed != null) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new ItemStyleRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(index, removed.getStyleType())));
            }
         }
         return removed;
    }

    //Get methods which return wrappers around the ItemStyles


    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getEndNodeStyle() {
        NodeStyle style = super.getEndNodeStyle();
        return style == null ? null : new ServerMessageSendingNodeStyle(style, messagingCell, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getEndSegmentStyle() {
        SegmentStyle style = super.getEndSegmentStyle();
        return style == null ? null : new ServerMessageSendingSegmentStyle(style, messagingCell, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getNodeStyle(int index, boolean relativeToNodes) throws IndexOutOfBoundsException {
        NodeStyle style = super.getNodeStyle(index, relativeToNodes);
        return style == null ? null : new ServerMessageSendingNodeStyle(style, messagingCell, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getSegmentStyle(int index, boolean relativeToSegments) throws IndexOutOfBoundsException {
        SegmentStyle style = super.getSegmentStyle(index, relativeToSegments);
        return style == null ? null : new ServerMessageSendingSegmentStyle(style, messagingCell, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getStartNodeStyle() {
        NodeStyle style = super.getStartNodeStyle();
        return style == null ? null : new ServerMessageSendingNodeStyle(style, messagingCell, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getStartSegmentStyle() {
        SegmentStyle style = super.getStartSegmentStyle();
        return style == null ? null : new ServerMessageSendingSegmentStyle(style, messagingCell, this);
    }

    /**
     * This class is a wrapper around a SegmentStyle which sends update messages to the
     * server if the SegmentStyle is changed.
     */
    private static class ServerMessageSendingSegmentStyle extends SegmentStyle implements StyleAttributeChangeListener {

        private final Cell messagingCell;
        private final SegmentStyle wrappedStyle;
        private final PathStyle parentStyle;

        /**
         * Create a new instance of a ServerMessageSendingSegmentStyle to wrap a SegmentStyle and send messages
         * to the server when the SegmentStyle changes.
         *
         * @param wrappedStyle The SegmentStyle which is to be wrapped by this object and which contains the actual mode.
         * @param messagingCell The Cell which is used to send messages to the server.
         * @param parentStyle The PathStyle which is the parent of this SegmentStyle.
         * @throws IllegalArgumentException If either the wrapped SegmentStyle or the messaging Cell was null.
         */
        public ServerMessageSendingSegmentStyle(final SegmentStyle wrappedStyle, final Cell messagingCell, final PathStyle parentStyle) throws IllegalArgumentException {
            super(wrappedStyle);
            if (messagingCell == null) {
                throw new IllegalArgumentException("The messaging cell through which server update messages are to be sent was null!");
            }
            if (parentStyle == null) {
                throw new IllegalArgumentException("The parent PathStyle for the server message sending segment style cannot be null!");
            }
            this.wrappedStyle = wrappedStyle;
            this.messagingCell = messagingCell;
            this.parentStyle = parentStyle;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute getStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
            StyleAttribute attribute = super.getStyleAttribute(attributeIndex);
            return attribute != null ? attribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute getStyleAttribute(String attributeName) {
            StyleAttribute attribute = super.getStyleAttribute(attributeName);
            return attribute != null ? attribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute[] getStyleAttributes() {
            StyleAttribute[] attributes = super.getStyleAttributes();
            StyleAttribute current = null;
            for (int attributeIndex = 0; attributeIndex < attributes.length; attributeIndex++) {
                current = attributes[attributeIndex];
                if (current != null) {
                    attributes[attributeIndex] = current.listeningWrapper(this);
                }
            }
            return attributes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setStyleType(SegmentStyleType styleType) {
            if (styleType != getStyleType()) {
                super.setStyleType(styleType);
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new ItemStyleTypeChangeMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleType));
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addStyleAttribute(StyleAttribute styleAttribute) {
            if (super.addStyleAttribute(styleAttribute)) {
               ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
               if (channelComponent != null) {
                   channelComponent.send(new StyleAttributeAddedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleAttribute));
               }
               return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeAllAttributes() {
            super.removeAllAttributes();
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new AllStyleAttributesRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType())));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeStyleAttribute(StyleAttribute styleAttribute) {
            int attributeIndex = indexOf(styleAttribute);
            if (attributeIndex >= 0 && super.removeStyleAttribute(attributeIndex) != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeIndex));
                }
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute removeStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
            StyleAttribute removedAttribute = super.removeStyleAttribute(attributeIndex);
            if (removedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeIndex));
                }
            }
            return removedAttribute != null ? removedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute removeStyleAttribute(String attributeName) {
            StyleAttribute removedAttribute = super.removeStyleAttribute(attributeName);
            if (removedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeName));
                }
            }
            return removedAttribute != null ? removedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute replaceStyleAttribute(StyleAttribute styleAttribute) {
            StyleAttribute replacedAttribute = super.replaceStyleAttribute(styleAttribute);
            if (replacedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeReplacedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleAttribute));
                }
            }
            return replacedAttribute != null ? replacedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSpan(int span) throws IllegalArgumentException {
            if (span != getSpan()) {
                super.setSpan(span);
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new ItemStyleSpanChangeMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), span));
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void attributeChanged(StyleAttribute source) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new StyleAttributeChangedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), source));
            }
        }
    }

    /**
     * This class is a wrapper around a NodeStyle which sends update messages to the
     * server if the NodeStyle is changed.
     */
    private static class ServerMessageSendingNodeStyle extends NodeStyle implements StyleAttributeChangeListener {

        private final Cell messagingCell;
        private final NodeStyle wrappedStyle;
        private final PathStyle parentStyle;

        /**
         * Create a new instance of a ServerMessageSendingNodeStyle to wrap a NodeStyle and send messages
         * to th server when the NodeStyle changes.
         *
         * @param wrappedStyle The NodeStyle which is to be wrapped by this object and which contains the actual mode.
         * @param messagingCell The Cell which is used to send messages to the server.
         */
        public ServerMessageSendingNodeStyle(NodeStyle wrappedStyle, Cell messagingCell, PathStyle parentStyle) {
            super(wrappedStyle);
            if (messagingCell == null) {
                throw new IllegalArgumentException("The messaging cell through which server update messages are to be sent was null!");
            }
            this.messagingCell = messagingCell;
            this.wrappedStyle = wrappedStyle;
            this.parentStyle = parentStyle;
            final int noOfAttributes = wrappedStyle.noOfAttributes();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setStyleType(NodeStyleType styleType) {
            if (styleType != getStyleType()) {
                super.setStyleType(styleType);
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new ItemStyleTypeChangeMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleType));
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute getStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
            StyleAttribute attribute = super.getStyleAttribute(attributeIndex);
            return attribute != null ? attribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute getStyleAttribute(String attributeName) {
            StyleAttribute attribute = super.getStyleAttribute(attributeName);
            return attribute != null ? attribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute[] getStyleAttributes() {
            StyleAttribute[] attributes = super.getStyleAttributes();
            StyleAttribute current = null;
            for (int attributeIndex = 0; attributeIndex < attributes.length; attributeIndex++) {
                current = attributes[attributeIndex];
                if (current != null) {
                    attributes[attributeIndex] = current.listeningWrapper(this);
                }
            }
            return attributes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addStyleAttribute(StyleAttribute styleAttribute) {
            if (super.addStyleAttribute(styleAttribute)) {
               ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
               if (channelComponent != null) {
                   channelComponent.send(new StyleAttributeAddedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleAttribute));
               }
               return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeAllAttributes() {
            super.removeAllAttributes();
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new AllStyleAttributesRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType())));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeStyleAttribute(StyleAttribute styleAttribute) {
            int attributeIndex = indexOf(styleAttribute);
            if (attributeIndex >= 0 && super.removeStyleAttribute(attributeIndex) != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeIndex));
                }
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute removeStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
            StyleAttribute removedAttribute = super.removeStyleAttribute(attributeIndex);
            if (removedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeIndex));
                }
            }
            return removedAttribute != null ? removedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute removeStyleAttribute(String attributeName) {
            StyleAttribute removedAttribute = super.removeStyleAttribute(attributeName);
            if (removedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeRemovedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), attributeName));
                }
            }
            return removedAttribute != null ? removedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StyleAttribute replaceStyleAttribute(StyleAttribute styleAttribute) {
            StyleAttribute replacedAttribute = super.replaceStyleAttribute(styleAttribute);
            if (replacedAttribute != null) {
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new StyleAttributeReplacedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), styleAttribute));
                }
            }
            return replacedAttribute != null ? replacedAttribute.listeningWrapper(this) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSpan(int span) throws IllegalArgumentException {
            if (span != getSpan()) {
                super.setSpan(span);
                ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
                if (channelComponent != null) {
                    channelComponent.send(new ItemStyleSpanChangeMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), span));
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void attributeChanged(StyleAttribute source) {
            ChannelComponent channelComponent = messagingCell.getComponent(ChannelComponent.class);
            if (channelComponent != null) {
                channelComponent.send(new StyleAttributeChangedMessage(messagingCell.getCellID(), new ItemStyleIdentifier(parentStyle.indexOf(wrappedStyle), wrappedStyle.getStyleType()), source));
            }
        }
    }
}
