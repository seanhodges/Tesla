python -c "import dbus; bus = dbus.SessionBus(); proxy = bus.get_object('org.freedesktop.Notifications', '/org/freedesktop/Notifications'); iface = dbus.Interface(proxy, dbus_interface='org.freedesktop.Notifications'); iface.Notify('test', 100, '/usr/share/icons/gnome/32x32/apps/calc.png', 'test title', 'test body', [], {}, 1)"