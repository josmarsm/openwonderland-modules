package org.jdesktop.wonderland.modules.path.client.ui;

import com.jme.math.Ray;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class is a simple Runnable designed to run on the AWT thread to display dialogs to the user for making choices about the position of the new node.
 */
public class HeightChoiceDialogRunner implements Runnable, Disposable {

    private static final String SAME_ABSOLUTE_HEIGHT_OPTION = "Same Absolute Height";
    private static final String SAME_RELATIVE_HEIGHT_OPTION = "Same Relative Height";
    private static final String SELECTED_HEIGHT_OPTION = "Selected Height";
    private static final String AVATAR_HEIGHT_OPTION = "Avatar Height";
    private static final String DEFAULT_HEIGHT_OPTION = "Default Height";
    private static final String GROUND_HEIGHT_OPTION = "Ground Height";
    private static final String CUSTOM_HEIGHT_OPTION = "Custom Height";

    private static final Object[] CREATION_OPTIONS_AT_SELECTION = new Object[] { DEFAULT_HEIGHT_OPTION, SELECTED_HEIGHT_OPTION, GROUND_HEIGHT_OPTION, CUSTOM_HEIGHT_OPTION };
    private static final Object[] CREATION_OPTIONS_AT_AVATAR = new Object[] { DEFAULT_HEIGHT_OPTION, AVATAR_HEIGHT_OPTION, GROUND_HEIGHT_OPTION, CUSTOM_HEIGHT_OPTION };
    private static final Object[] MOVE_OPTIONS_AT_SELECTION = new Object[] { SAME_ABSOLUTE_HEIGHT_OPTION, SAME_RELATIVE_HEIGHT_OPTION, DEFAULT_HEIGHT_OPTION, SELECTED_HEIGHT_OPTION, GROUND_HEIGHT_OPTION, CUSTOM_HEIGHT_OPTION };
    private static final Object[] MOVE_OPTIONS_AT_AVATAR = new Object[] { SAME_ABSOLUTE_HEIGHT_OPTION, SAME_RELATIVE_HEIGHT_OPTION, DEFAULT_HEIGHT_OPTION, AVATAR_HEIGHT_OPTION, GROUND_HEIGHT_OPTION, CUSTOM_HEIGHT_OPTION };
    private static final Object CREATION_DEFAULT_OPTION = DEFAULT_HEIGHT_OPTION;
    private static final Object MOVE_DEFAULT_OPTION = SAME_RELATIVE_HEIGHT_OPTION;

    private static final float DEFAULT_AVATAR_OFFSET = 0.5f;
    private static final float DEFAULT_HEIGHT = 0.5f;

    protected static final Logger logger = Logger.getLogger(HeightChoiceDialogRunner.class.getName());

    /**
     * Get the Cell which represents the Avatar.
     * 
     * @return The Avatar cell.
     */
    public static Cell getAvatarCell() {
        return ClientContextJME.getViewManager().getPrimaryViewCell();
    }

    /**
     * Try and get the outer JFrame for the Wonderland Client application.
     *
     * @return The JFrame for the Wonderland Client.
     */
    public static JFrame tryGetParentFrame() {
        return (ClientContext.getRendererType() == RendererType.RENDERER_JME) ? JmeClientMain.getFrame().getFrame() : null;
    }

    /**
     * Get the Avatar position (offset as necessary).
     * 
     * @param offset The amount to offset the avatar position i.e. to select a point in from of the avatar offset by that amount.
     * @param transform The transform used to adjust the global avatar position to local coordinates (if applicable) or null if the coordinates do not need to be transformed.
     * @return The position of the Avatar, offset if requested.
     */
    public static Vector3f getAvatarPosition(float offset, CellTransform transform) {
        Vector3f avatarPosition = new Vector3f();
        Cell avatarCell = getAvatarCell();
        if (avatarCell != null) {
            CellTransform avatar = avatarCell.getWorldTransform();
            Vector3f avatarLookVector = new Vector3f();
            avatar.getLookAt(avatarPosition, avatarLookVector);
            logger.warning(String.format("Start avatar position: (%f, %f, %f) and look direction: (%f, %f, %f).", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookVector.x, avatarLookVector.y, avatarLookVector.z));
            if (offset != 0.0f) {
                avatarLookVector.multLocal(offset);
                logger.warning(String.format("Offset vector: (%f, %f, %f).", avatarLookVector.x, avatarLookVector.y, avatarLookVector.z));
                avatarPosition.addLocal(avatarLookVector);
                logger.warning(String.format("Position after offset: (%f, %f, %f).", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            }
            if (transform != null) {
                avatarPosition = transform.transform(avatarPosition);
                logger.warning(String.format("Transformed avatar position: (%f, %f, %f).", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            }
        }
        return avatarPosition;
    }

    /**
     * Find the global y position of the ground level.
     *
     * @param position The global 3D position for which to find the ground level between it.
     * @param collisionSystem The collisionSystem to use to find objects which can act as ground below the specified position.
     * @return The global y position of the ground level.
     */
    public static float findGroundLevel(Vector3f position, CollisionSystem collisionSystem) {
        if (position != null && collisionSystem != null) {
            float yDelta = 1.0f;
            float groundYOffset = 0.0f;
            Ray heightRay = new Ray();
            heightRay.origin.set(position.x, position.y + yDelta, position.z);
            heightRay.direction.set(0.0f, -1.0f, 0.0f);
            PickInfo pickInfo = collisionSystem.pickAllWorldRay(heightRay, true, false);
            final int noOfPickedItems = pickInfo.size();
            if (pickInfo.size() > 0) {
                // Grab the first collidable
                PickDetails firstCollidablePickDetails = null;
                for (int pickDetailsIndex = 0; pickDetailsIndex < noOfPickedItems; pickDetailsIndex++) {
                    PickDetails currentPickDetails = pickInfo.get(pickDetailsIndex);
                    if (currentPickDetails.getCollisionComponent().isCollidable()) {
                        firstCollidablePickDetails = currentPickDetails;
                        break;
                    }
                }
                if (firstCollidablePickDetails != null) {
                    groundYOffset = firstCollidablePickDetails.getDistance() - yDelta;
                    return position.y - groundYOffset;
                }
            }
        }
        //No ground, return 0.
        return 0.0f;
    }

    /**
     * Show a height selection dialog using the Avatar position as the basis for the position.
     *
     * @param positionable The Positionable which will receive notification when a position / height is selected.
     * @param transform The cell transform to use to transform the destination position as needed. This can be null if no transformation
     *                  is necessary.
     * @throws IllegalArgumentException If the specified Positionable was null.
     */
    public static void showHeightSelectionUsingAvatar(Positionable positionable, CellTransform transform) throws IllegalArgumentException {
        showHeightSelectionUsingAvatar(positionable, null, transform);
    }

    /**
     * Show a height selection dialog using the Avatar position as the basis for the position.
     *
     * @param positionable The Positionable which will receive notification when a position / height is selected.
     * @param origin The original position of the item (which can be null if not applicable).
     * @param transform The cell transform to use to transform the destination position as needed. This can be null if no transformation
     *                  is necessary.
     * @throws IllegalArgumentException If the specified Positionable was null.
     */
    public static void showHeightSelectionUsingAvatar(Positionable positionable, Vector3f origin, CellTransform transform) throws IllegalArgumentException {
        Vector3f destination = getAvatarPosition(DEFAULT_AVATAR_OFFSET, transform);
        CollisionSystem collisionSystem = ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        HeightChoiceDialogRunner runner = new HeightChoiceDialogRunner(positionable, destination, findGroundLevel(destination, collisionSystem), true, origin, findGroundLevel(origin, collisionSystem));
        javax.swing.SwingUtilities.invokeLater(runner);
    }

    private Positionable positionable;
    private final Vector3f destination;
    private final boolean fromAvatar;
    private final float destinationGroundLevel;
    private final Vector3f origin;
    private final float startGroundLevel;

    /**
     * Create a new HeightChoiceDialogRunner to run presenting the user with the available options about the destination height for the moved or created item.
     *
     * @param positionable The Positionable item which will be notified of the selected position.
     * @param destination The destination position which was selected.
     * @param fromAvatar Whether this destination position originated from the Avatar position (which affects the presented options).
     * @param origin The original position of the item if it is moving or null if it is not.
     * @throws IllegalArgumentException If the Positionable or destination was null.
     */
    public HeightChoiceDialogRunner(final Positionable positionable, final Vector3f destination, final float destinationGroundLevel, final boolean fromAvatar, final Vector3f origin, final float originGroundLevel) throws IllegalArgumentException {
        if (positionable == null) {
            throw new IllegalArgumentException("The positionalble to be informed of the selected position cannot be null!");
        }
        if (destination == null) {
            throw new IllegalArgumentException("The specified destination for the ");
        }
        this.positionable = positionable;
        this.fromAvatar = fromAvatar;
        this.destination = destination;
        this.destinationGroundLevel = destinationGroundLevel;
        if (origin != null) {
            //ToDo Calculate This.
            startGroundLevel = originGroundLevel;
            this.origin = origin.clone();
        }
        else {
            startGroundLevel = 0.0f;
            this.origin = null;
        }
    }

    /**
     * Run the height selection. This should execute on the Swing thread.
     */
    @Override
    public void run() {
        Object[] options;
        Object defaultOption;
        if (origin == null) {
            if (fromAvatar) {
                options = CREATION_OPTIONS_AT_AVATAR;
            }
            else {
                options = CREATION_OPTIONS_AT_SELECTION;
            }
            defaultOption = CREATION_DEFAULT_OPTION;
        }
        else {
            if (fromAvatar) {
                options = MOVE_OPTIONS_AT_AVATAR;
            }
            else {
                options = MOVE_OPTIONS_AT_SELECTION;
            }
            defaultOption = MOVE_DEFAULT_OPTION;
        }
        
        Object selection = javax.swing.JOptionPane.showInputDialog(tryGetParentFrame(), "Which value should be used for the destination height?", "Select Destination Height.", javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, defaultOption);
        if (selection != null) {
            if (SAME_RELATIVE_HEIGHT_OPTION.equals(selection)) {
                positionable.usePosition(destination.x, destinationGroundLevel + (origin.y - startGroundLevel), destination.z);
            }
            else if (SAME_ABSOLUTE_HEIGHT_OPTION.equals(selection)) {
                positionable.usePosition(destination.x, origin.y, destination.z);
            }
            else if (SELECTED_HEIGHT_OPTION.equals(selection) || AVATAR_HEIGHT_OPTION.equals(selection)) {
                positionable.usePosition(destination.x, destination.y, destination.z);
            }
            else if (DEFAULT_HEIGHT_OPTION.equals(selection)) {
                positionable.usePosition(destination.x, destinationGroundLevel + DEFAULT_HEIGHT, destination.z);
            }
            else if (GROUND_HEIGHT_OPTION.equals(selection)) {
                positionable.usePosition(destination.x, destinationGroundLevel, destination.z);
            }
            else if (CUSTOM_HEIGHT_OPTION.equals(selection)) {
                //Show height selection dialog.
            }
        }
        dispose();
    }

    @Override
    public void dispose() {
        positionable = null;
    }
}
