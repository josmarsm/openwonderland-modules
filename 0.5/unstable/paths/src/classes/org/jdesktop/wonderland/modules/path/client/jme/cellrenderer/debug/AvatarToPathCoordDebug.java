package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.debug;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.ui.DebugOutputDialog;

/**
 * Class containing some debugging code to check the different ways in which coordinates can be transformed from the Avatar to Path coordinate spaces.
 *
 * @author Carl Jokl
 */
public class AvatarToPathCoordDebug {

    public static void debugAvatarToPathCoords(Cell avatarCell, PathCell pathCell) {
        if (avatarCell != null && pathCell != null) {
            ClientNodePath path = pathCell.getNodePath();
            //Avatar position test:
            DebugOutputDialog debugDialog = DebugOutputDialog.createDialog();
            Vector3f avatarPosition = new Vector3f();
            Vector3f avatarLookAt = new Vector3f();
            Vector3f pathMin = new Vector3f();
            Vector3f pathMax = new Vector3f();
            Vector3f pathPosition = new Vector3f();
            Vector3f pathLookAt = new Vector3f();
            path.getCoordRange(pathMin, pathMax);
            CellTransform currentAvatarTransform = avatarCell.getWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Raw Avatar World Translation: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            currentAvatarTransform = avatarCell.getLocalTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Raw Avatar Local Translation: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            currentAvatarTransform = avatarCell.getLocalToWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Raw Avatar Local to World Translation: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local to World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //Path
            debugDialog.append(String.format("Path node range from: (%f, %f, %f) to: (%f, %f, %f).\n", pathMin.x, pathMin.y, pathMin.z, pathMax.x, pathMax.y, pathMax.z));
            //Path World
            CellTransform currentPathTransform = pathCell.getWorldTransform();
            pathPosition = currentPathTransform.getTranslation(pathPosition);
            debugDialog.append(String.format("Raw Path World Translation: (%f, %f, %f)\n", pathPosition.x, pathPosition.y, pathPosition.z));
            currentPathTransform.getLookAt(pathPosition, pathLookAt);
            debugDialog.append(String.format("Path World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", pathPosition.x, pathPosition.y, pathPosition.z, pathLookAt.x, pathLookAt.y, pathLookAt.z));
            //From Avatar World
            currentAvatarTransform = avatarCell.getWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar World Translation: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local
            currentAvatarTransform = avatarCell.getLocalTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local Translation: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local to World
            currentAvatarTransform = avatarCell.getLocalToWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local To World Translation: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local To World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //Path Local
            currentPathTransform = pathCell.getLocalTransform();
            pathPosition = currentPathTransform.getTranslation(pathPosition);
            debugDialog.append(String.format("Raw Path Local Translation: (%f, %f, %f)\n", pathPosition.x, pathPosition.y, pathPosition.z));
            currentPathTransform.getLookAt(pathPosition, pathLookAt);
            debugDialog.append(String.format("Path Local Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", pathPosition.x, pathPosition.y, pathPosition.z, pathLookAt.x, pathLookAt.y, pathLookAt.z));
            //From Avatar World
            currentAvatarTransform = avatarCell.getWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar World Translation: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f)\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local
            currentAvatarTransform = avatarCell.getLocalTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local Translation: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local to World
            currentAvatarTransform = avatarCell.getLocalToWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local To World Translation: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local To World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //Path Local to World
            currentPathTransform = pathCell.getLocalToWorldTransform();
            pathPosition = currentPathTransform.getTranslation(pathPosition);
            debugDialog.append(String.format("Raw Path Local to World Translation: (%f, %f, %f)\n", pathPosition.x, pathPosition.y, pathPosition.z));
            currentPathTransform.getLookAt(pathPosition, pathLookAt);
            debugDialog.append(String.format("Path Local to World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", pathPosition.x, pathPosition.y, pathPosition.z, pathLookAt.x, pathLookAt.y, pathLookAt.z));
            //From Avatar World
            currentAvatarTransform = avatarCell.getWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar World Translation: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f)\n.", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local
            currentAvatarTransform = avatarCell.getLocalTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local Translation: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            //From Avatar Local to World
            currentAvatarTransform = avatarCell.getLocalToWorldTransform();
            avatarPosition = currentAvatarTransform.getTranslation(avatarPosition);
            debugDialog.append(String.format("Transforming Raw Avatar Local To World Translation: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            debugDialog.append(String.format("is converted to: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z));
            currentAvatarTransform.getLookAt(avatarPosition, avatarLookAt);
            debugDialog.append(String.format("Avatar Local To World Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f) through Path Local to World Transform\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));
            avatarPosition = currentPathTransform.transform(avatarPosition);
            avatarLookAt = currentPathTransform.transform(avatarLookAt);
            debugDialog.append(String.format("is converted to Look At Position: (%f, %f, %f) and Forward Vector: (%f, %f, %f).\n", avatarPosition.x, avatarPosition.y, avatarPosition.z, avatarLookAt.x, avatarLookAt.y, avatarLookAt.z));

            //HeightChoiceDialogRunner.showHeightSelectionUsingAvatar(new NodeMovePositionable(node.getSequenceIndex(), path), node.getPosition(), cell.getWorldTransform());
        }
    }
}
