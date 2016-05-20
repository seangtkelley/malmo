package com.microsoft.Malmo.MissionHandlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import com.microsoft.Malmo.MalmoMod;
import com.microsoft.Malmo.MissionHandlerInterfaces.ICommandHandler;
import com.microsoft.Malmo.Schemas.DiscreteMovementCommand;
import com.microsoft.Malmo.Schemas.MissionInit;

/** Fairly dumb command handler that attempts to move the player one block N,S,E or W.<br> */
public class DiscreteMovementCommandsImplementation extends CommandBase implements ICommandHandler
{
    public static final String MOVE_ATTEMPTED_KEY = "attemptedToMove";

    private boolean isOverriding;
    private int direction = -1;
    
	@Override
	protected boolean onExecute(String verb, String parameter, MissionInit missionInit)
	{
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null)
        {
            if (this.direction == -1)
            {
                // Initialise direction:
                this.direction = (int)((player.rotationYaw + 45.0f) / 90.0f);
                this.direction = (this.direction + 4) % 4;
            }
            int z = 0;
            int x = 0;
            int y = 0;
            if (verb.equalsIgnoreCase(DiscreteMovementCommand.MOVENORTH.value()))
            {
                z = -1;
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.MOVESOUTH.value()))
            {
                z = 1;
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.MOVEEAST.value()))
            {
                x = 1;
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.MOVEWEST.value()))
            {
                x = -1;
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.JUMP.value()))
            {
                y = 1;
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.MOVE.value()))
            {
                if (parameter != null && parameter.length() != 0)
                {
                    float velocity = Float.valueOf(parameter);
                    int offset = (velocity > 0) ? 1 : ((velocity < 0) ? -1 : 0);
                    switch (this.direction)
                    {
                    case 0: // North
                        z = offset; break;
                    case 1: // East
                        x = -offset; break;
                    case 2: // South
                        z = -offset; break;
                    case 3: // West
                        x = offset; break;
                    }
                }
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.TURN.value()))
            {
                if (parameter != null && parameter.length() != 0)
                {
                    float yawDelta = Float.valueOf(parameter);
                    this.direction += (yawDelta > 0) ? 1 : -1;
                    this.direction = (this.direction + 4) % 4;
                    player.rotationYaw = this.direction * 90;
                    player.onUpdate();
                }
            }
            else if (verb.equalsIgnoreCase(DiscreteMovementCommand.LOOK.value()))
            {
            	if (parameter != null && parameter.length() != 0)
            	{
            		float pitchDelta = Float.valueOf(parameter);
            		player.rotationPitch += (pitchDelta < 0) ? -45 : 45;
                    player.onUpdate();
            	}
            }

            if (z != 0 || x != 0 || y != 0)
            {
                // Attempt to move the entity:
                player.moveEntity(x,  y,  z);
                player.onUpdate();
                // Now check where we ended up:
                double newX = player.posX;
                double newZ = player.posZ;
                // Are we still in the centre of a square, or did we get shunted?
                double desiredX = (int)newX + 0.5;
                double desiredZ = (int)newZ + 0.5;
                double deltaX = desiredX - newX;
                double deltaZ = desiredZ - newZ;
                if (deltaX * deltaX + deltaZ * deltaZ > 0.001)
                {
                    // Need to re-centralise:
                    player.moveEntity(deltaX, 0, deltaZ);
                    player.onUpdate();
                }
                // Now set the last tick pos values, to turn off inter-tick positional interpolation:
                player.lastTickPosX = player.posX;
                player.lastTickPosY = player.posY;
                player.lastTickPosZ = player.posZ;

                try
                {
					MalmoMod.getPropertiesForCurrentThread().put(MOVE_ATTEMPTED_KEY, true);
				}
                catch (Exception e)
                {
                	// TODO - proper error reporting.
                	System.out.println("Failed to access properties for the client thread after discrete movement - reward may be incorrect.");
				}
                return true;
            }
        }
        return false;
	}

	@Override
	public void install(MissionInit missionInit)
	{
	}

	@Override
	public void deinstall(MissionInit missionInit)
	{
	}

    @Override
    public boolean isOverriding()
    {
        return this.isOverriding;
    }

    @Override
    public void setOverriding(boolean b)
    {
        this.isOverriding = b;
    }
}