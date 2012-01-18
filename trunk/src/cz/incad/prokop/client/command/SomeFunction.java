package cz.incad.prokop.client.command;

import org.aplikator.client.command.BindableCommand;
import org.aplikator.client.widgets.TextFieldWidget;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

@SuppressWarnings("serial")
public class SomeFunction extends BindableCommand {

    public SomeFunction() {
        super();
    }

    @Override
    public void execute() {
        if (contents == null) {
            VerticalPanel vSplit = new VerticalPanel();
            // vSplit.setSize("600px", "400px");
            vSplit.add(new TextFieldWidget<String>("WizardField1", null, false, null));
            vSplit.add(new TextFieldWidget<String>("WizardField2", null, false, null));
            vSplit.add(new Button("Execute"));
            SimplePanel decPanel = new SimplePanel();
            // decPanel.setSize("800px", "450px");
            decPanel.setWidget(vSplit);
            contents = decPanel;
        }
        mainPanel.setContents(contents);
        // mainPanel.add(decPanel, tabName);
        // mainPanel.selectTab(mainPanel.getWidgetCount()-1);

    }

}
