/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package slim.test.bare;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.OptionBooleanModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

/**
 *
 * @author Matthias Mann
 */
public class PreviewWidgets extends DialogLayout {

    public PreviewWidgets() {
        Label label = new Label("Label");
        Button button = new Button("Button");
        ToggleButton toggleButton = new ToggleButton("ToggleButton");
        ToggleButton checkBox = new ToggleButton("CheckBox");
        checkBox.setTheme("checkbox");
        ComboBox<String> comboBox = new ComboBox<String>(
                new SimpleChangableListModel<String>("Test", "Hello World", "End"));
        EditField editField = new EditField();
        
        Scrollbar scrollbarH = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        Scrollbar scrollbarV = new Scrollbar(Scrollbar.Orientation.VERTICAL);

        ToggleButton[] radioButtons = new ToggleButton[4];
        IntegerModel model = new SimpleIntegerModel(0, radioButtons.length-1, 0);
        for(int i=0 ; i<radioButtons.length ; i++) {
            radioButtons[i] = new ToggleButton(new OptionBooleanModel(model, i));
            radioButtons[i].setText("Opt " + (i+1));
            radioButtons[i].setTheme("radiobutton");
        }

        Button[] vbuttons = new Button[4];
        for(int i=0 ; i<vbuttons.length ; i++) {
            vbuttons[i] = new Button();
            vbuttons[i].setTheme("vbutton");
        }

        Widget[] widgets = new Widget[] {
            label, button, toggleButton, checkBox
        };
        Group horzWidgets = createParallelGroup();
        for(Widget w : widgets) {
            horzWidgets.addGroup(createSequentialGroup().addWidget(w).addGap());
        }
        horzWidgets.addWidget(comboBox).addWidget(editField);
        horzWidgets.addGroup(createSequentialGroup().addWidgetsWithGap("radiobutton", radioButtons));

        Group vertWidgets = createSequentialGroup(widgets)
                .addWidget(comboBox)
                .addWidget(editField)
                .addGroup(createParallelGroup(radioButtons))
                .addGap();

        setHorizontalGroup(createSequentialGroup()
                .addGroup(createParallelGroup().addGroup(horzWidgets).addWidget(scrollbarH))
                .addGroup(createParallelGroup(vbuttons))
                .addWidget(scrollbarV));
        setVerticalGroup(createSequentialGroup()
                .addGroup(createParallelGroup()
                    .addGroup(vertWidgets)
                    .addWidget(scrollbarV)
                    .addGroup(createSequentialGroup().addWidgetsWithGap("vbutton", vbuttons).addGap()))
                .addWidget(scrollbarH));
    }

}