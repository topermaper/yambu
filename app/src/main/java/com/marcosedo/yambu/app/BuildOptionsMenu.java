package com.marcosedo.yambu.app;

import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.marcosedo.yambu.R;

/**
 * Created by Marcos on 12/03/15.
 */
public class BuildOptionsMenu {


    private Menu optionsMenu;
    private boolean[] options;

    BuildOptionsMenu(Menu menu, boolean[] showOptions) {
        optionsMenu = menu;
        options = showOptions;
        show();
    }

    BuildOptionsMenu() {

    }

    public void setMenu(Menu menu) {
        optionsMenu = menu;
    }

    public void setOptions(boolean[] showOptions) {
        options = showOptions;
    }

    public void remove(int id){
        optionsMenu.removeItem(id);
    }

    public void show() {

        MenuItem menuItem;
        optionsMenu.clear();

        if (options[0]) {//0 es para el boton de añadir
            //add MenuItem(s) to ActionBar using Java code
            menuItem = optionsMenu.add(1, R.id.action_add_event, 0, "Add event");
            menuItem.setIcon(R.drawable.ic_menu_add);
            menuItem.setTitle("Add event");
            MenuItemCompat.setShowAsAction(menuItem,
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        if (options[1]) {//1 es para el boton de opciones de lista
            //add MenuItem(s) to ActionBar using Java code
            menuItem = optionsMenu.add(1, R.id.action_eventslist_options, 1, "List options");
            menuItem.setIcon(R.drawable.ic_menu_sort_by_size);
            MenuItemCompat.setShowAsAction(menuItem,
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        if (options[2]) {//0 es para el boton de añadir grupo
            //add MenuItem(s) to ActionBar using Java code
            menuItem = optionsMenu.add(1, R.id.action_add_group, 2, "Add new group");
            menuItem.setIcon(R.drawable.ic_menu_add);
            MenuItemCompat.setShowAsAction(menuItem,
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }


        menuItem = optionsMenu.add(1, R.id.action_logout, 10, "Logout");
        //menuItem.setIcon(R.drawable.yambu32);
        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        optionsMenu.add(1, R.id.action_listaGrupos, 4, "My groups");
        //optionsMenu.add(1, R.id.action_notificaciones, 5, "Notifications");//esto ya no se usa tampoco
        //optionsMenu.add(1, R.id.action_messages,6,"Messages");//esto no se va a usar de momento
        optionsMenu.add(1, R.id.action_listaEventos, 7, "Events");
        optionsMenu.add(1,R.id.action_preferences,8,"Preferences");
        optionsMenu.add(1, R.id.action_help, 9, "Help");
    }
}