package nl.pim16aap2.bigdoors.gui;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.doors.DoorType;
import nl.pim16aap2.bigdoors.gui.GUI.SortType;
import nl.pim16aap2.bigdoors.spigotutil.PageType;
import nl.pim16aap2.bigdoors.spigotutil.SpigotUtil;
import nl.pim16aap2.bigdoors.util.messages.Message;
import nl.pim16aap2.bigdoors.util.messages.Messages;

import java.util.ArrayList;

class GUIPageDoorList implements IGUIPage
{
    protected final BigDoors plugin;
    protected final GUI gui;
    protected final Messages messages;

    GUIPageDoorList(final BigDoors plugin, final GUI gui)
    {
        this.plugin = plugin;
        this.gui = gui;
        messages = plugin.getMessages();
        refresh();
    }

    @Override
    public PageType getPageType()
    {
        return PageType.DOORLIST;
    }

    @Override
    public void handleInput(int interactionIDX)
    {
        if (interactionIDX == 0)
        {
            gui.setPage(gui.getPage() - 1);
            gui.update();
        }
        else if (interactionIDX == 1)
        {
            gui.setNextSortType();
            gui.update();
        }
        else if (interactionIDX == 4)
            gui.setGUIPage(new GUIPageDoorCreation(plugin, gui));
        else if (interactionIDX == 8)
        {
            gui.setPage(gui.getPage() + 1);
            gui.update();
        }
        else
        {
            gui.setDoor(gui.getItem(interactionIDX).getDoor());
            if (gui.getDoor() == null)
            {
                SpigotUtil.messagePlayer(gui.getPlayer(), messages.getString(Message.ERROR_GENERALERROR));
                gui.close();
                return;
            }
            if (gui.isStillOwner())
                gui.setGUIPage(new GUIPageDoorInfo(plugin, gui));
        }
    }

    protected void fillHeader()
    {
        int page = gui.getPage();
        ArrayList<String> lore = new ArrayList<>();
        if (page != 0)
        {
            lore.add(plugin.getMessages().getString(Message.GUI_DESCRIPTION_PREVIOUSPAGE,
                                                    Integer.toString(gui.getPage() + 2),
                                                    Integer.toString(gui.getPage()),
                                                    Integer.toString(gui.getMaxPageCount())));
            gui.addItem(0, new GUIItem(GUI.PAGESWITCHMAT,
                                       plugin.getMessages().getString(Message.GUI_BUTTON_PREVIOUSPAGE), lore,
                                       Math.max(1, gui.getPage())));
            lore.clear();
        }

        lore.add(messages.getString(SortType.getMessage(gui.getSortType())));
        gui.addItem(1, new GUIItem(GUI.TOGGLEDOORMAT, messages.getString(Message.GUI_BUTTON_SORT), lore, 1));
        lore.clear();

        gui.addItem(4, new GUIItem(GUI.NEWDOORMAT, messages.getString(Message.GUI_BUTTON_NEW), null, 1));

        if ((page + 1) < gui.getMaxPageCount())
        {
            lore.add(plugin.getMessages().getString(Message.GUI_DESCRIPTION_NEXTPAGE,
                                                    Integer.toString(gui.getPage() + 2),
                                                    Integer.toString(gui.getPage()),
                                                    Integer.toString(gui.getMaxPageCount())));
            gui.addItem(0, new GUIItem(GUI.PAGESWITCHMAT, plugin.getMessages().getString(Message.GUI_BUTTON_NEXTPAGE),
                                       lore, page + 2));
            lore.clear();
        }
    }

    protected void fillPage()
    {
        int offset = gui.getPage() * (GUI.CHESTSIZE - 9);
        int endCount = Math.min((GUI.CHESTSIZE - 9), (gui.getDoorsSize() - offset));
        ArrayList<String> lore = new ArrayList<>();
        for (int idx = 0; idx < endCount; ++idx)
        {
            DoorBase currentDoor = gui.getDoor(offset + idx);
            DoorType doorType = currentDoor.getType();
            lore.add(messages.getString(Message.GUI_DESCRIPTION_DOORID, Long.toString(currentDoor.getDoorUID())));
            lore.add(messages.getString(DoorType.getMessage(doorType)));
            GUIItem item = new GUIItem(GUI.DOORTYPES[DoorType.getValue(doorType)], currentDoor.getName(), lore, 1);
            item.setDoor(currentDoor);
            gui.addItem(idx + 9, item);
            lore.clear();
        }
    }

    @Override
    public void refresh()
    {
        fillHeader();
        fillPage();
    }
}
