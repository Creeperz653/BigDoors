package nl.pim16aap2.bigdoors.spigot.gui;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.AddOwner;
import nl.pim16aap2.bigdoors.commands.Info;
import nl.pim16aap2.bigdoors.commands.MovePowerBlock;
import nl.pim16aap2.bigdoors.commands.SetAutoCloseTime;
import nl.pim16aap2.bigdoors.commands.SetBlocksToMove;
import nl.pim16aap2.bigdoors.commands.Toggle;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.util.PageType;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.messages.IMessages;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Deprecated
@SuppressWarnings("NullAway")
public class GUIPageDoorInfo implements IGUIPage
{
    protected final BigDoorsSpigot plugin;
    protected final GUI gui;
    protected final IMessages messages;

    protected GUIPageDoorInfo(final BigDoorsSpigot plugin, final GUI gui)
    {
        this.plugin = plugin;
        this.gui = gui;
        messages = plugin.getMessages();
        refresh();
    }

    @Override
    public void kill()
    {
    }

    @Override
    public PageType getPageType()
    {
        return PageType.DOORINFO;
    }

    /**
     * Handles user input for an action that they have the required level of ownership for for this door in the database
     * manager.
     *
     * @param door           The door.
     * @param player         The player.
     * @param guiItem        The GUIItem pressed.
     * @param interactionIDX The index of the GUIItem the player interacted with.
     */
    private void handleAllowedInput(final @NotNull AbstractDoorBase door, final @NotNull Player player,
                                    final @NotNull GUIItem guiItem, final int interactionIDX)
    {
        guiItem.getDoorAttribute().ifPresent(
            attr ->
            {
                switch (attr)
                {
                    case LOCK:
                        door.setLocked(!door.isLocked()).syncData();
                        gui.updateItem(interactionIDX, createGUIItemOfAttribute(door, DoorAttribute.LOCK));
                        break;
                    case TOGGLE:
                        Toggle.run(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        break;
                    case SWITCH: // TODO: Implement door pausing.
                        break;
                    case INFO:
                        Info.run(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        break;
                    case DELETE:
                        gui.setGUIPage(new GUIPageDeleteConfirmation(plugin, gui));
                        break;
                    case RELOCATE_POWERBLOCK:
                        MovePowerBlock.run(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        gui.close();
                        break;
                    case OPEN_DIRECTION:
                        changeOpenDir(door, interactionIDX);
                        break;
                    case AUTO_CLOSE_TIMER:
                        SetAutoCloseTime.runDelayed(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        gui.close();
                        break;
                    case BLOCKS_TO_MOVE:
                        SetBlocksToMove.runDelayed(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        gui.close();
                        break;
                    case ADD_OWNER:
                        AddOwner.runDelayed(SpigotAdapter.wrapPlayer(player), DoorRetriever.of(door));
                        gui.close();
                        break;
                    case REMOVE_OWNER:
                        switchToRemoveOwner();
                }
            });
    }

    @Override
    public void handleInput(int interactionIDX)
    {
        if (interactionIDX == 0)
        {
            gui.setGUIPage(new GUIPageDoorList(plugin, gui));
            return;
        }
        // Only button in the header is the back button.
        if (interactionIDX < 9)
            return;

        GUIItem guiItem = gui.getItem(interactionIDX);
        if (guiItem.getDoorAttribute().isEmpty())
            return;


        if (!Util.hasPermissionForAction(gui.getGuiHolder(), gui.getDoor(), guiItem.getDoorAttribute().get()))
        {
            gui.update();
            return;
        }
        BigDoors.get().getPlatform().getPExecutor().runOnMainThread(
            () -> handleAllowedInput(gui.getDoor(), SpigotAdapter.getBukkitPlayer(gui.getGuiHolder()), guiItem,
                                     interactionIDX));
    }

    protected void fillHeader()
    {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessages().getString(Message.GUI_DESCRIPTION_PREVIOUSPAGE,
                                                Integer.toString(gui.getPage() + 2),
                                                Integer.toString(gui.getPage() + 1),
                                                Integer.toString(gui.getMaxPageCount())));
        gui.addItem(0, new GUIItem(GUI.PAGESWITCHMAT,
                                   plugin.getMessages().getString(Message.GUI_BUTTON_PREVIOUSPAGE), lore,
                                   Math.max(1, gui.getPage())));
        lore.clear();

        lore.add(messages.getString(Message.GUI_DESCRIPTION_INFO, gui.getDoor().getName()));
        lore.add(messages.getString(Message.GUI_DESCRIPTION_DOORID, Long.toString(gui.getDoor().getDoorUID())));
//        lore.add(messages.getString(EDoorType.getMessage(gui.getDoor().getType())));
        lore.add(gui.getDoor().getDoorType().getTranslationName());
        gui.addItem(4, new GUIItem(GUI.CURRDOORMAT, gui.getDoor().getName() + ": " + gui.getDoor().getDoorUID(),
                                   lore, 1));
    }

    protected void fillPage()
    {
//        final AtomicInteger position = new AtomicInteger(8);
//        for (DoorAttribute attr : EDoorType.getAttributes(gui.getDoor().getType()))
//            createGUIItemOfAttribute(gui.getDoor(), attr).ifPresent(I -> gui.addItem(position.addAndGet(1), I));
    }

    @Override
    public void refresh()
    {
        fillHeader();
        fillPage();
    }

    private void switchToRemoveOwner()
    {
//        // Text based method
//        plugin.getCommander().startRemoveOwner(gui.getOfflinePlayer(), gui.getDoor());
//        gui.close();

        // GUI based method
        gui.setGUIPage(new GUIPageRemoveOwner(plugin, gui));
    }

    // Changes the opening direction for a door.
    private void changeOpenDir(final @NotNull AbstractDoorBase door, final int index)
    {
//        DoorAttribute[] attributes = EDoorType.getAttributes(door.getType());
//        DoorAttribute openTypeAttribute = null;
//
//        outerLoop:
//        for (int idx = 0; idx != attributes.length; ++idx)
//        {
//            switch (attributes[idx])
//            {
//                case DIRECTION_ROTATE_HORIZONTAL:
//                    openTypeAttribute = DoorAttribute.DIRECTION_ROTATE_HORIZONTAL;
//                    break outerLoop;
//                case DIRECTION_ROTATE_VERTICAL:
//                    openTypeAttribute = DoorAttribute.DIRECTION_ROTATE_VERTICAL;
//                    break outerLoop;
//
//                case DIRECTION_ROTATE_VERTICAL2:
//                    openTypeAttribute = DoorAttribute.DIRECTION_ROTATE_VERTICAL2;
//                    break outerLoop;
//                case DIRECTION_STRAIGHT_HORIZONTAL:
//                    openTypeAttribute = DoorAttribute.DIRECTION_STRAIGHT_HORIZONTAL;
//                    break outerLoop;
//                case DIRECTION_STRAIGHT_VERTICAL:
//                    openTypeAttribute = DoorAttribute.DIRECTION_STRAIGHT_VERTICAL;
//                    break outerLoop;
//                default:
//                    break;
//            }
//        }
//        RotateDirection newOpenDir = door.cycleOpenDirection();
//
//        BigDoors.get().getDatabaseManager().updateDoorOpenDirection(door.getDoorUID(), newOpenDir);
//        int idx = gui.indexOfDoor(door);
//        gui.getDoor(idx).setOpenDir(newOpenDir);
//        gui.setDoor(gui.getDoor(idx));
//        // TODO: Check this.
//        gui.updateItem(index, createGUIItemOfAttribute(door, openTypeAttribute));
    }

    private @NotNull Optional<GUIItem> createGUIItemOfAttribute(final @NotNull AbstractDoorBase door,
                                                                final @NotNull DoorAttribute atr)
    {
//        // If the permission level is higher than the max permission of this action.
//        if (door.getPermission() > DoorAttribute.getPermissionLevel(atr))
//            return Optional.empty();
//
//        List<String> lore = new ArrayList<>();
//        String desc;
//        GUIItem ret = null;
//
//        switch (atr)
//        {
//            case LOCK:
//                if (door.isLocked())
//                    ret = new GUIItem(GUI.LOCKDOORMAT, messages.getString(Message.GUI_BUTTON_LOCK), null, 1);
//                else
//                    ret = new GUIItem(GUI.UNLOCKDOORMAT, messages.getString(Message.GUI_BUTTON_UNLOCK), null, 1);
//                break;
//
//            case TOGGLE:
//                desc = messages.getString(Message.GUI_BUTTON_TOGGLE);
//                ret = new GUIItem(GUI.TOGGLEDOORMAT, desc, lore, 1);
//                break;
//
//            case INFO:
//                desc = messages.getString(Message.GUI_BUTTON_INFO);
//                ret = new GUIItem(GUI.INFOMAT, desc, lore, 1);
//                break;
//
//            case DELETE:
//                desc = messages.getString(Message.GUI_BUTTON_DOOR_DELETE);
//                lore.add(messages.getString(Message.GUI_DESCRIPTION_DOOR_DELETE));
//                ret = new GUIItem(GUI.DELDOORMAT, desc, lore, 1);
//                break;
//
//            case RELOCATEPOWERBLOCK:
//                desc = messages.getString(Message.GUI_BUTTON_RELOCATEPB);
//                ret = new GUIItem(GUI.RELOCATEPBMAT, desc, lore, 1);
//                break;
//
//            case CHANGETIMER:
//                desc = messages.getString(Message.GUI_BUTTON_TIMER);
//                lore.add(door.getAutoClose() > -1 ?
//                         messages.getString(Message.GUI_DESCRIPTION_TIMER_SET, Integer.toString(door.getAutoClose())) :
//                         messages.getString(Message.GUI_DESCRIPTION_TIMER_NOTSET));
//                int count = Math.max(1, door.getAutoClose());
//                ret = new GUIItem(GUI.CHANGETIMEMAT, desc, lore, count);
//                break;
//
//            case DIRECTION_ROTATE_VERTICAL2:
//            case DIRECTION_STRAIGHT_HORIZONTAL:
//            case DIRECTION_STRAIGHT_VERTICAL:
//            case DIRECTION_ROTATE_VERTICAL:
//                desc = messages.getString(Message.GUI_BUTTON_DIRECTION);
//                lore.add(messages.getString(Message.GUI_DESCRIPTION_OPENDIRECTION,
//                                            messages.getString(RotateDirection.getMessage(
//                                                door.getOpenDir()))));
//                ret = new GUIItem(GUI.SETOPENDIRMAT, desc, lore, 1);
//                break;
//
//            case DIRECTION_ROTATE_HORIZONTAL:
//                desc = messages.getString(Message.GUI_BUTTON_DIRECTION);
//                lore.add(messages.getString(Message.GUI_DESCRIPTION_OPENDIRECTION_RELATIVE,
//                                            messages.getString(
//                                                RotateDirection.getMessage(door.getOpenDir())),
//                                            messages.getString(RotateDirection.getMessage(RotateDirection.DOWN))));
//                ret = new GUIItem(GUI.SETOPENDIRMAT, desc, lore, 1);
//                break;
//
//            case BLOCKSTOMOVE:
//                desc = messages.getString(Message.GUI_BUTTON_BLOCKSTOMOVE);
//                lore.add(messages.getString(Message.GUI_DESCRIPTION_BLOCKSTOMOVE,
//                                            Integer.toString(door.getBlocksToMove())));
//                ret = new GUIItem(GUI.SETBTMOVEMAT, desc, lore, 1);
//                break;
//
//            case ADDOWNER:
//                desc = messages.getString(Message.GUI_BUTTON_OWNER_ADD);
//                ret = new GUIItem(GUI.ADDOWNERMAT, desc, lore, 1);
//                break;
//
//            case REMOVEOWNER:
//                desc = messages.getString(Message.GUI_BUTTON_OWNER_DELETE);
//                ret = new GUIItem(GUI.REMOVEOWNERMAT, desc, lore, 1);
//                break;
//            default:
//                break;
//        }
//        if (ret != null)
//            ret.setDoorAttribute(atr);
//        return Optional.ofNullable(ret);
        return Optional.empty();
    }
}
