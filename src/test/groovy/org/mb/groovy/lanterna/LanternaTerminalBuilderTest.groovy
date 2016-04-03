package org.mb.groovy.lanterna

import com.googlecode.lanterna.input.KeyStroke
import org.junit.Ignore
import org.junit.Test

public class LanternaTerminalBuilderTest {
    @Ignore @Test
    public void testBasics() {
        def terminal = new LanternaTerminalBuilder().terminal(title: 'Main Frame') {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    gridLayout(2) {
                        label('My First Little Label')
                        panel(borderStyle: 'singleLine', borderTitle: 'BorderLayout') {
                            borderLayout {
                                top {
                                    label 'TOP'
                                }
                                right {
                                    label 'RIGHT'
                                }
                                bottom {
                                    label 'BOTTOM'
                                }
                                left {
                                    label 'LEFT'
                                }
                                center {
                                    label 'CENTER'
                                }
                            }
                        }
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testBasicAlternative() {
        def terminal = new LanternaTerminalBuilder().terminal() {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    gridLayout(2) {
                        label('My First Little Label')
                        label('And a second one')
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testComponentRegistration() {
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    gridLayout(2) {
                        label(id: 'label1', 'My First Little Label')
                        label(id: 'label2', 'And a second one')
                    }
                }
            }
            window(id: 'subWindow', title: 'Sub Window', visible: false) {
                panel {
                    linearLayout {
                        label(id: 'label1', 'Label 1')
                        label(id: 'label2', 'Label 2')
                    }
                }
            }
        }

        assert terminal.windows['mainWindow'].components.collect { k, v -> k } == ['label1', 'label2']
        assert terminal.windows['subWindow'].components.collect { k, v -> k } == ['label1', 'label2']
    }

    @Ignore @Test
    public void testTextBox() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    linearLayout() {
                        textBox(id: 'tb1', size: 20, 'Hello World')
                        textBox(size: 20, components.tb1.text)
                        textBox(size: [20,2], 'Hello\n  World')
                        textBox(size: 20, validationPattern: '[0-9]+', '12345A')
                        // the above should throw an exception as per the Lanterna docs,
                        // but it doesn't
                        textBox(size: 20, password: true)
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testButton() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    linearLayout() {
                        textBox(id: 'tb1', size: 20, 'Value to print')
                        label('Click below')
                        button('Click Me') {
                            println components.tb1.text
                        }
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testComboBox() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    gridLayout(2) {
                        def theCollection = ['One', 'Two', 'Three']
                        label('collection with size')
                        comboBox(size: [10, 4], theCollection)
                        label('collection with id')
                        comboBox(id: 'theBox', theCollection)
                        label('collection with action')
                        comboBox(theCollection) { selected, previous ->
                            println "selected index: $selected"
                        }
                        label('writeable collection with action')
                        comboBox(writeable: true, theCollection) { selected, previous ->
                            println "read only: selected index: $selected"
                        }
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testEmptySpace() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'Empty Space Test') {
                panel {
                    gridLayout(2) {
                        label('label 1')
                        emptySpace()
                        label('label 2, should show under label 1')
                        label('label3')
                        emptySpace()
                        label('Should show under label 3')
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testCheckBox() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'CheckBox Test') {
                panel {
                    gridLayout(2) {
                        checkBox('Simple')
                        checkBox(size: [15,2], 'With Size')
                        checkBox(checked: true, 'Checked')
                        checkBox(checked: false, 'Unchecked')
                        checkBox('With Action') { checked ->
                            println "Box is checked: $checked"
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testCheckBoxList() {
        // minimal theme OK, could be better with different scrollbar chars
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'CheckBoxList Test') {
                panel {
                    gridLayout(2) {
                        label('Size 15x2')
                        def l1 = ['Item 1', 'Item 2', 'Item 3']
                        checkBoxList(size: [15, 2], borderStyle: 'singleLine',  l1)
                        label('No Size')
                        checkBoxList(l1)
                        label('With Action')
                        checkBoxList(l1) { idx, checked ->
                            println "item at index $idx is ${checked ? 'checked' : 'unchecked'}"
                        }
                        label('With item 1 & 3 checked')
                        checkBoxList(checked: [l1[0], l1[2]], l1)
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testRadioBoxList() {
        // minimal theme OK, could be better with different scrollbar chars
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'CheckBoxList Test') {
                panel {
                    gridLayout(2) {
                        label('Size 15x2')
                        def items = ['Item 1', 'Item 2', 'Item 3']
                        radioBoxList(size: [15, 2], items)
                        label('No Size')
                        radioBoxList(items)
                        label('With Action')
                        radioBoxList(items) { selected, previous ->
                            println "item at index ${selected} is selected"
                        }
                        label('With item 2 checked')
                        radioBoxList(checked: items[1], items)
                        label('With index 2 checked (Item 3)')
                        radioBoxList(checkedByIndex: 2, items)
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testActionListBox() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'ActionListBox Test') {
                panel {
                    gridLayout(2) {
                        label(id: 'label1', 'Size 15x2')
                        actionListBox {
                            action('Action 1') {
                                println 'Action 1'
                            }
                            action('Action 2') {
                                println 'Action 2'
                            }
                            action('Action 3') {
                                println "Text of Label: ${components.label1.text}"
                            }
                        }

                        label(id: 'label2', 'No Size')
                        actionListBox {
                            action('Action 1') {
                                println 'Action 1'
                            }
                            action('Action 2') {
                                println 'Action 2'
                            }
                            action('Action 3') {
                                println "Text of Label: ${components.label2.text}"
                            }
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testMessageDialog() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'MessageDialog Test') {
                panel {
                    linearLayout {
                        button('Click me for an OK dialog') {
                            println terminal.messageDialog(title: 'OK dialog', 'Should say OK')
                        }
                        button('Click me for a Cancel dialog') {
                            println terminal.messageDialog(button: 'Cancel', title: 'Cancel dialog', 'Should say Cancel')
                        }
                        button('Click me for a dialog with 2 buttons') {
                            println terminal.messageDialog(buttons: ['OK', 'Cancel'], 'Should say OK and Cancel')
                        }
                    }
                }
            }
        }

        terminal.messageDialog(title: 'Started', 'Application started')
        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testTextInputDialog() {
        // minimal theme OK
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'TextInputDialog Test') {
                panel {
                    linearLayout {
                        button('Click me for an input dialog') {
                            println terminal.textInputDialog(title: 'Input', description: 'Free-form input', size: [30, 10])
                        }
                        button('Click me for a number input dialog') {
                            println terminal.textInputDialog(
                                title: 'Number input',
                                description: 'Numbers only please!',
                                validationPattern: '[0-9]*',
                                errorMessage: 'Thats not a number!'
                            )
                        }
                        button('Click me for a password dialog') {
                            println terminal.textInputDialog(
                                title: 'Password',
                                description: 'Enter password',
                                password: true
                            )
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testFileDialog() {
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'FileDialog Test') {
                panel {
                    linearLayout {
                        button('Click me for a file dialog') {
                            def file = terminal.fileDialog(new File('C:/'))
                            println file.absolutePath
                        }
                        button('Click me for a more complex file dialog') {
                            def file = terminal.fileDialog(
                                title: 'Select a file bro!',
                                description: 'Mak sure its a good file!',
                                actionLabel: 'Go for it!',
                                showHidden: true,
                                size: [70, 24], // preferred size
                                new File('C:/')
                            )
                            println file.absolutePath
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testActionListDialog() {
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'ActionListDialog Test') {
                panel {
                    linearLayout {
                        button('Click me for an action list dialog') {
                            terminal.actionListDialog {
                                action('Print foobar') {
                                    println 'foobar'
                                }
                                action('Print barfoo') {
                                    println 'barfoo'
                                }
                            }
                        }
                        button('Click me for a more complex action list dialog') {
                            terminal.actionListDialog(
                                title: 'Foobar Printer',
                                description: 'Allows you to print something',
                                size: [20, 5]
                            ) {
                                action('Print foobar') {
                                    println 'foobar'
                                }
                                action('Print barfoo') {
                                    println 'barfoo'
                                }
                            }
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testOnKey() {
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'ActionListDialog Test') {
                panel {
                    linearLayout {
                        label('Label 1')
                        button('Open Dialog') {
                            terminal.messageDialog(title: 'Model Dialog', 'Some Message')
                        }
                    }
                }
            }
            onKey { KeyStroke keyStroke ->
                println "Key pressed: ${keyStroke.character}"
            }
        }

        terminal.waitFor('main')
    }

    @Ignore @Test
    public void testMinimalTheme() {
        def terminal = new LanternaTerminalBuilder().terminal(title: 'Main Frame', bareTerminal: true) {
            window(id: 'mainWindow', title: 'My First Window') {
                panel {
                    gridLayout(2) {
                        label('My First Little Label')
                        panel(borderStyle: 'singleLine', borderTitle: 'BorderLayout') {
                            borderLayout {
                                top {
                                    label 'TOP'
                                }
                                right {
                                    label 'RIGHT'
                                }
                                bottom {
                                    label 'BOTTOM'
                                }
                                left {
                                    label 'LEFT'
                                }
                                center {
                                    label 'CENTER'
                                }
                            }
                        }
                    }
                }
            }
        }

        terminal.waitFor('mainWindow')
    }

    @Ignore @Test
    public void testLinearLayout() {
        def terminal = new LanternaTerminalBuilder().terminal(bareTerminal: true) {
            window(id: 'main', title: 'ActionListDialog Test') {
                panel {
                    linearLayout {
                        panel {
                            linearLayout(direction: 'vertical') {
                                label('very long label to ensure we have room to align the following labels')
                                label(align: 'center', 'Center')
                                label(align: 'beginning', 'Beginning')
                                label(align: 'end', 'End')
                                label(align: 'fill', 'Fill')
                            }
                        }
                        panel {
                            linearLayout(direction: 'horizontal') {
                                label('left')
                                label('right')
                            }
                        }
                    }
                }
            }
        }

        terminal.waitFor('main')
    }
}