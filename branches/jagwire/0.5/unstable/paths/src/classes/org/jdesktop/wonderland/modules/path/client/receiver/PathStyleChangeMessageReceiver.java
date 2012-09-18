package org.jdesktop.wonderland.modules.path.client.receiver;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.message.AllStyleAttributesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleAppendedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleSpanChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.ItemStyleTypeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathStyleChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.StyleAttributeReplacedMessage;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;

/**
 * This class is intended to receive PathStyleChangeMessages from the server.
 *
 * @author Carl Jokl
 */
public class PathStyleChangeMessageReceiver extends PathCellComponentMessageReceiver {

    /**
     * Create a new instance of a PathStyleChangeMessageReceiver to receive PathStyleChangedMessages.
     *
     * @param cell The Cell for which this message receiver is to receive messages.
     * @param path The NodePath which is to be modified and updated by the messages received.
     */
    public PathStyleChangeMessageReceiver(Cell cell, ClientNodePath path) {
        super(cell, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(CellMessage message) {
        if (handeMessage(message.getSenderID())) {
            if (message instanceof PathStyleChangeMessage) {
                path.setPathStyle(((PathStyleChangeMessage) message).getPathStyle());
            }
            else {
                PathStyle style = path.getPathStyle();
                if (style != null) {
                    if (message instanceof AllStyleAttributesRemovedMessage) {
                        AllStyleAttributesRemovedMessage allAttributesRemovedMessage = (AllStyleAttributesRemovedMessage) message;
                        ItemStyle itemStyle = allAttributesRemovedMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            itemStyle.removeAllAttributes();
                        }
                    }
                    else if (message instanceof ItemStyleAppendedMessage) {
                        ItemStyleAppendedMessage styleAppendedMessage = (ItemStyleAppendedMessage) message;
                        ItemStyle itemStyle = styleAppendedMessage.getItemStyle();
                        if (itemStyle instanceof NodeStyle) {
                            style.append((NodeStyle) itemStyle);
                        }
                        else if (itemStyle instanceof SegmentStyle) {
                            style.append((SegmentStyle) itemStyle);
                        }
                    }
                    else if (message instanceof ItemStyleInsertedMessage) {
                        ItemStyleInsertedMessage styleInsertedMessage = (ItemStyleInsertedMessage) message;
                        ItemStyle itemStyle = styleInsertedMessage.getItemStyle();
                        if (itemStyle instanceof NodeStyle) {
                            style.insertAt(styleInsertedMessage.getIndex(), (NodeStyle) itemStyle);
                        }
                        else if (itemStyle instanceof SegmentStyle) {
                            style.insertAt(styleInsertedMessage.getIndex(), (SegmentStyle) itemStyle);
                        }
                    }
                    else if (message instanceof ItemStyleRemovedMessage) {
                        ItemStyleRemovedMessage styleRemovedMessage = (ItemStyleRemovedMessage) message;
                        styleRemovedMessage.getItemStyleID().removeItemStyle(style);
                    }
                    else if (message instanceof ItemStyleSpanChangeMessage) {
                        ItemStyleSpanChangeMessage spanChangeMessage = (ItemStyleSpanChangeMessage) message;
                        ItemStyle itemStyle = spanChangeMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            itemStyle.setSpan(spanChangeMessage.getSpan());
                        }
                    }
                    else if (message instanceof ItemStyleTypeChangeMessage) {
                        ItemStyleTypeChangeMessage styleTypeChangeMessage = (ItemStyleTypeChangeMessage) message;
                        ItemStyle itemStyle = styleTypeChangeMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            itemStyle.setStyleType(styleTypeChangeMessage.getStyleType());
                        }
                    }
                    else if (message instanceof StyleAttributeAddedMessage) {
                        StyleAttributeAddedMessage attributeAddedMessage = (StyleAttributeAddedMessage) message;
                        ItemStyle itemStyle = attributeAddedMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            itemStyle.addStyleAttribute(attributeAddedMessage.getAddedAttribute());
                        }
                    }
                    else if (message instanceof StyleAttributeRemovedMessage) {
                        StyleAttributeRemovedMessage attributeRemovedMessage = (StyleAttributeRemovedMessage) message;
                        ItemStyle itemStyle = attributeRemovedMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            attributeRemovedMessage.getID().removeAttributeFrom(itemStyle);
                        }
                    }
                    else if (message instanceof StyleAttributeReplacedMessage) {
                        StyleAttributeReplacedMessage attributeReplacedMessage = (StyleAttributeReplacedMessage) message;
                        ItemStyle itemStyle = attributeReplacedMessage.getItemStyleID().getItemStyle(style);
                        if (itemStyle != null) {
                            itemStyle.replaceStyleAttribute(attributeReplacedMessage.getReplacementAttribute());
                        }
                    }
                }
            }
        }
    }

}
