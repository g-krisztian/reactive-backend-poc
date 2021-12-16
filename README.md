# Reactive backend POC

The basic idea is to figure out if we can use atom's watcher to handle a subscription based backend.

Actions are key/function pairs. The handler creates an atom, and adds watchers with all function what defined in router,
and resets the atom with the content of the request.

The watchers are reacting to the changed values, calling the subscribed functions in order of route definition. The
execution results are merged back to the watched atom. Anytime a key changes in the atom, the subscribed actions are
executed. The handler reacts to the `:response` key, extracting it, and sends it back to the caller.

## Usage

FIXME

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which
is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary Licenses when the conditions for such
availability set forth in the Eclipse Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your option) any later version, with the GNU
Classpath Exception which is available at https://www.gnu.org/software/classpath/license.html.
