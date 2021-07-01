package nl.pim16aap2.bigdoors.spigot.gui;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.util.PageType;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.api.util.messages.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@SuppressWarnings("NullAway")
public class GUIPageDoorCreation implements IGUIPage
{
    protected final BigDoorsSpigot plugin;
    protected final GUI gui;
    protected final Object subCommand;

    GUIPageDoorCreation(final @NotNull BigDoorsSpigot plugin, final @NotNull GUI gui)
    {
        this.plugin = plugin;
        this.gui = gui;
        subCommand = null;
        refresh();
    }

    @Override
    public void kill()
    {

    }

    @Override
    public @NotNull PageType getPageType()
    {
        return PageType.DOORCREATION;
    }

    @Override
    public void handleInput(int interactionIDX)
    {
        if (interactionIDX == 0)
        {
            gui.setGUIPage(new GUIPageDoorList(plugin, gui));
            return;
        }
        GUIItem item = gui.getItem(interactionIDX);
        if (item == null)
            return;

        if (!(item.getSpecialValue() instanceof DoorType))
        {
            plugin.getPLogger().warn("Something went wrong constructing the selected GUIItem at " + interactionIDX
                                         + ":\n" + item.toString());
            return;
        }
        startCreationProcess(SpigotAdapter.getBukkitPlayer(gui.getGuiHolder()), (DoorType) item.getSpecialValue());
    }

    @Override
    public void refresh()
    {
        fillHeader();
        fillPage();
    }

    private void fillHeader()
    {
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessages().getString(Message.GUI_DESCRIPTION_PREVIOUSPAGE,
                                                Integer.toString(gui.getPage() + 2),
                                                Integer.toString(gui.getPage() + 1),
                                                Integer.toString(gui.getMaxPageCount())));
        gui.addItem(0, new GUIItem(GUI.PAGESWITCHMAT,
                                   plugin.getMessages().getString(Message.GUI_BUTTON_PREVIOUSPAGE), lore,
                                   Math.max(1, gui.getPage())));
    }

    private void fillPage()
    {
        int position = 9;
        for (DoorType type : BigDoors.get().getDoorTypeManager().getSortedDoorTypes())
            if (BigDoors.get().getDoorTypeManager().isDoorTypeEnabled(type))
//                SubCommandNew.hasCreationPermission(SpigotAdapter.getBukkitPlayer(gui.getGuiHolder()), type))
            {
                String initMessage = plugin.getMessages().getString(Message.GUI_DESCRIPTION_INITIATION,
                                                                    plugin.getMessages()
                                                                          .getString(type.getTranslationName()));
                gui.addItem(position++,
                            new GUIItem(GUI.NEWDOORMAT, initMessage, null, 1, type));
            }

    }

    private void startCreationProcess(final @NotNull Player player, final @NotNull DoorType type)
    {
        player.closeInventory();
//        subCommand.execute(player, null, type);
    }
}
