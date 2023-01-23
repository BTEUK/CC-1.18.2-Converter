package me.bteuk.converterplugin.utils.blocks.stairs;

import me.bteuk.converterplugin.utils.Direction;
import me.bteuk.converterplugin.utils.Facing;
import org.bukkit.Location;

public class StairData {

    int facing;
    Location l;
    int d;
    Shape sh;

    //Main stair constructor.
    public void StairData(Facing facing, Location l) {
        this.l = l;
        this.facing = facingNumber(facing);

        //Default
        this.d = 0;
    }

    //Alt stair constructor.
    public void StairData(Facing facing, Location l, StairData s) {
        this.l = l;
        this.facing = facingNumber(facing);

        //Set direction and shape in relation to main stair.
        setDirection(s);
        setShape(s);
    }

    //Set shape of alt stair based on main stair.
    public void setShape(StairData s) {
        if ((d == 1 || d == 3) && (facing == s.facing)) {
            sh = Shape.STRAIGHT;
        } else if (d == 2 && (facing % 2) != (s.facing % 2)) {
            sh = Shape.ICORNER;
        } else if (d == 0 && (facing % 2) != (s.facing % 2)) {
            sh = Shape.OCORNER;
        } else {
            sh = Shape.NONE;
        }
    }

    //Set direction of alt stair in relation to main stair.
    public void setDirection(StairData s) {
        switch (s.facing) {
            //Negative Z
            case 1 -> {
                if (this.l.getZ() < s.l.getZ()) {
                    d = directionNumber(Direction.UP);
                } else if (this.l.getZ() > s.l.getZ()) {
                    d = directionNumber(Direction.DOWN);
                } else if (this.l.getX() < s.l.getX()) {
                    d = directionNumber(Direction.LEFT);
                } else {
                    d = directionNumber(Direction.RIGHT);
                }
            }
            //Positive X
            case 2 -> {
                if (this.l.getZ() < s.l.getZ()) {
                    d = directionNumber(Direction.LEFT);
                } else if (this.l.getZ() > s.l.getZ()) {
                    d = directionNumber(Direction.RIGHT);
                } else if (this.l.getX() < s.l.getX()) {
                    d = directionNumber(Direction.DOWN);
                } else {
                    d = directionNumber(Direction.UP);
                }
            }
            //Positive Z
            case 0 -> {
                if (this.l.getZ() > s.l.getZ()) {
                    d = directionNumber(Direction.UP);
                } else if (this.l.getZ() < s.l.getZ()) {
                    d = directionNumber(Direction.DOWN);
                } else if (this.l.getX() > s.l.getX()) {
                    d = directionNumber(Direction.LEFT);
                } else {
                    d = directionNumber(Direction.RIGHT);
                }
            }
            //Negative X
            case 3 -> {
                if (this.l.getZ() > s.l.getZ()) {
                    d = directionNumber(Direction.LEFT);
                } else if (this.l.getZ() < s.l.getZ()) {
                    d = directionNumber(Direction.RIGHT);
                } else if (this.l.getX() > s.l.getX()) {
                    d = directionNumber(Direction.DOWN);
                } else {
                    d = directionNumber(Direction.UP);
                }
            }
        }
    }

    //Convert direction to a number for convenience.
    public int directionNumber(Direction d) {
        switch (d) {
            case LEFT -> {return 1;}
            case UP -> {return 2;}
            case RIGHT -> {return 3;}
            default -> {return 0;}
        }
    }

    //Set facing direction to a number for convenience.
    public int facingNumber(Facing f) {
        switch (f) {
            case NORTH -> {return 0;}
            case EAST -> {return 1;}
            case WEST -> {return 3;}
            default -> {return 2;}
        }
    }

    //Get the shape of the main stair based on the 4 adjacent stairs.
    public Shape getShape(StairData[] sd) {

        /*
        1. If contains Straight
            - Check for 2nd Straight ->
                return Straight
            - Check for CornerI ->
                if CornerI[facing] != Straight[direction] ->
                    return CornerI in Straight[direction]
            - Check for CornerO ->
                if CornerO[facing] == Straight[direction] ->
                    return CornerO in !Straight[direction]
            - Else return Straight

        2. If contains CornerI return CornerI in !CornerI[direction]
        3. If contains CornerO return CornerO in CornerO[direction]
         */

        StairData straight1 = null;
        StairData straight2 = null;
        StairData cornerI = null;
        StairData cornerO = null;

        for (StairData s : sd) {
            if (s.sh == Shape.STRAIGHT) {
                if (straight1 == null) {
                    straight1 = s;
                } else {
                    straight2 = s;
                }
            } else if (s.sh == Shape.ICORNER) {
                cornerI = s;
            } else if (s.sh == Shape.OCORNER) {
                cornerO = s;
            }
        }

        if (straight1 != null && straight2 != null) {
            return Shape.STRAIGHT;
        } else if (straight1 != null && cornerI != null) {
            if ((((cornerI.facing+1) % 4) == facing) && straight1.d == 1) {
                return Shape.INNER_LEFT;
            } else if ((((cornerI.facing+3) % 4) == facing) && straight1.d == 3) {
                return Shape.INNER_RIGHT;
            }
        } else if (straight1 != null && cornerO != null) {
            if ((((cornerO.facing + 1) % 4) == facing) && d == 3) {
                return Shape.OUTER_LEFT;
            } else if ((((cornerO.facing + 3) % 4) == facing) && straight1.d == 1) {
                return Shape.OUTER_RIGHT;
            }
        } else if (cornerI != null && ((cornerI.facing+1) % 4) == facing) {
            return Shape.INNER_LEFT;
        } else if (cornerI != null && ((cornerI.facing+3) % 4) == facing) {
            return Shape.INNER_RIGHT;
        } else if (cornerO != null && ((cornerO.facing+1) % 4) == facing) {
            return Shape.OUTER_LEFT;
        } else if (cornerO != null && ((cornerO.facing+3) % 4) == facing) {
            return Shape.OUTER_RIGHT;
        }

        return Shape.STRAIGHT;

    }
}
