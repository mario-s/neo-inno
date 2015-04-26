package org.inno.control;

import static com.google.common.collect.Lists.newArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

import javafx.stage.FileChooser;

import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.inno.export.ExporterFactory;
import org.inno.model.Project;
import org.inno.model.Technology;
import org.openide.util.Lookup;

/**
 * Controller to create the CYPHER statements.
 *
 * @author spindizzy
 */
public class MainController extends AbstractController implements LookupListener {

    private final BooleanProperty disableProperty;

    private final Result<org.inno.model.Node> result;

    @FXML
    private TextField txtFile;

    @FXML
    private Button btnCreate;

    @FXML
    private Tab relationTab;

    private File exportFile;

    public MainController() {
        disableProperty = new SimpleBooleanProperty(true);
        result = getContext().getLookup().lookupResult(org.inno.model.Node.class);
        result.addLookupListener(this);
    }

    @Override
    void initialize(final MessageFactory factory) {
        btnCreate.disableProperty().bind(disableProperty);
        relationTab.disableProperty().bind(disableProperty);
    }

    @FXML
    protected void handleFileSelection(final ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Output File");

        Node node = (Node) event.getSource();
        exportFile = fileChooser.showOpenDialog(node.getScene().getWindow());

        if (exportFile != null) {
            txtFile.setText(exportFile.getPath());
            resultChanged(null);
        }
    }

    @Override
    public void resultChanged(final LookupEvent le) {
        Collection<?> nodes = result.allInstances();
        disableProperty.set(exportFile == null || nodes.isEmpty());
    }

    @FXML
    void export(final ActionEvent event) {
        if (exportFile != null) {
            try {
                FileWriter writer = new FileWriter(exportFile);
                writer.write(createExportString());
            } catch (IOException exc) {
                getLogger().warn(exc.getMessage(), exc);
            }
        }
    }

    private String createExportString() {
        StringBuilder builder = new StringBuilder();
        ExporterFactory factory = new ExporterFactory();
        builder.append(factory.createExporter(Project.class).export(newArrayList(lookup(Project.class))));
        builder.append(factory.createExporter(Technology.class).export(newArrayList(lookup(Technology.class))));
        builder.append(factory.createRelationExporter().export((Map<Project, Set<Technology>>) lookup(HashMap.class)));
        
        return builder.toString();
    }

    private<T> Collection<? extends T> lookup(Class<T> clazz) {
        return getContext().getLookup().lookupAll(clazz);
    }
    
   
}
