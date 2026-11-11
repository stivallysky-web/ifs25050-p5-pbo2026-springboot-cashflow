package org.delcom.ifs25050springbootcashflow.framework.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImageZoomDialog extends Dialog {

    public ImageZoomDialog(String imageUrl, String altText) {
        setHeaderTitle("Preview Gambar");

        Image image = new Image(imageUrl, altText);
        image.setMaxWidth("80vw");
        image.setMaxHeight("70vh");
        image.getStyle().set("object-fit", "contain");
        image.getStyle().set("border-radius", "8px");

        VerticalLayout layout = new VerticalLayout(image);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setPadding(false);

        add(layout);

        Button closeButton = new Button("Tutup", e -> close());
        getFooter().add(closeButton);
    }
}