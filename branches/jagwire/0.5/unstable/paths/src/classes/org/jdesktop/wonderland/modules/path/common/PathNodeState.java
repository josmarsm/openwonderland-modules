package org.jdesktop.wonderland.modules.path.common;

import com.jme.math.Vector3f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This interface represents common functionality available in both client and server PathNodeCell state objects.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="path-node")
public class PathNodeState implements PathNode, Cloneable, Serializable {

    @XmlTransient
    private Vector3f position;

    @XmlTransient
    private String name;

    @XmlTransient
    private int sequenceIndex;

    /**
     * Create a new instance of a blank PathNodeState.
     * (No argument constructor for use with JAXB).
     */
    public PathNodeState() {}

    /**
     * Create a new PathNodeState
     *
     * @param position The position of the PathNode, if null a default zero vector will be created.
     * @param name The name of the PathNode (optional).
     */
    public PathNodeState(Vector3f position, String name) {
        this(position, name, 0);
    }

    /**
     * Create a new PathNodeState
     *
     * @param position The position of the PathNode, if null a default zero vector will be created.
     * @param name The name of the PathNode (optional).
     * @param sequenceIndex The index of the PathNode within the sequence of PathNodes in the path.
     */
    public PathNodeState(Vector3f position, String name, int sequenceIndex) {
        this.position = position != null ? position : new Vector3f();
        this.name = name;
        this.sequenceIndex = sequenceIndex;
    }


    /**
     * Create a new PathNodeState.
     *
     * @param x The x dimension position of the PathNode.
     * @param y The y dimension position of the PathNode.
     * @param z The z dimension position of the PathNode.
     * @param name The name of the PathNode (optional).
     */
    public PathNodeState(float x, float y, float z, String name) {
        this(new Vector3f(x, y, z), name, 0);
    }

    /**
     * Create a new PathNodeState.
     *
     * @param x The x dimension position of the PathNode.
     * @param y The y dimension position of the PathNode.
     * @param z The z dimension position of the PathNode.
     * @param name The name of the PathNode (optional).
     * @param sequenceIndex The index of the PathNode within the sequence of PathNodes in the path.
     */
    public PathNodeState(float x, float y, float z, String name, int sequenceIndex) {
        this(new Vector3f(x, y, z), name, sequenceIndex);
    }

    /**
     * {@inheritDoc}
     */
    @XmlAttribute(name="name")
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public boolean isNamed() {
        return name != null;
    }

    /**
     * {@inheritDoc}
     */
    @XmlAttribute(name="index")
    @Override
    public int getSequenceIndex() {
        return sequenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Get the x dimension component of the position of this PathNode.
     *
     * @return The x dimension component of the position of this PathNode.
     */
    @XmlAttribute(name="x")
    public float getX() {
        return position != null ? position.x : 0.0f;
    }

    /**
     * Set the x dimension component of the position of this PathNode.
     *
     * @param x The x dimension component of the position of this pathNode.
     */
    public void setX(float x) {
        if (position != null) {
            position.x = x;
        }
        else {
            position = new Vector3f(x, 0.0f, 0.0f);
        }
    }

    /**
     * Get the y dimension component of the position of this PathNode.
     *
     * @return The y dimension component of the position of this PathNode.
     */
    @XmlAttribute(name="y")
    public float getY() {
        return position != null ? position.y : 0.0f;
    }

    /**
     * Set the y dimension component of the position of this PathNode.
     *
     * @param y The y dimension component of the position of this PathNode.
     */
    public void setY(float y) {
        if (position != null) {
            position.y = y;
        }
        else {
            position = new Vector3f(0.0f, y, 0.0f);
        }
    }

    /**
     * Get the z dimension component of the position of this PathNode.
     *
     * @return The z dimension component of the position of this PathNode.
     */
    @XmlAttribute(name="z")
    public float getZ() {
        return position != null ? position.z : 0.0f;
    }

    /**
     * Set the z dimension component of the position of this PathNode.
     *
     * @param z The z dimension component of the position of this PathNode.
     */
    public void setZ(float z) {
        if (position != null) {
            position.z = z;
        }
        else {
            position = new Vector3f(0.0f, 0.0f, z);
        }
    }

    /**
     * Create a clone of this PathNodeState.
     *
     * @return A clone of this PathNodeState.
     */
    @Override
    public PathNodeState clone() {
        if (position != null) {
            return new PathNodeState(position.x, position.y, position.z, name, sequenceIndex);
        }
        else {
            return new PathNodeState(0.0f, 0.0f, 0.0f, name, sequenceIndex);
        }
    }
}
