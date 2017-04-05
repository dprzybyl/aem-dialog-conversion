package com.adobe.cq.dialogconversion.impl.rules;

import com.adobe.cq.dialogconversion.AbstractDialogRewriteRule;
import com.adobe.cq.dialogconversion.DialogRewriteException;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Set;

import static com.adobe.cq.dialogconversion.DialogRewriteUtils.copyProperty;
import static com.adobe.cq.dialogconversion.DialogRewriteUtils.hasXtype;
import static com.adobe.cq.dialogconversion.DialogRewriteUtils.rename;

/**
 * Rewrites widgets of xtype "multifield". The "fieldConfig" subnode (if existing) is renamed to "field" and
 * will be handled by subsequent passes of the algorithm.
 */
@Component
@Service
public class MultifieldRewriteRule extends AbstractDialogRewriteRule {

    private static final String XTYPE = "multifield";
    private static final String GRANITEUI_MULTIFIELD_RT = "granite/ui/components/coral/foundation/form/multifield";
    private static final String GRANITEUI_TEXTFIELD_RT = "granite/ui/components/coral/foundation/form/textfield";

    public boolean matches(Node root)
            throws RepositoryException {
        return hasXtype(root, XTYPE);
    }

    public Node applyTo(Node root, Set<Node> finalNodes)
            throws DialogRewriteException, RepositoryException {
        Node parent = root.getParent();
        String name = root.getName();
        rename(root);

        // add node for multifield
        Node newRoot = parent.addNode(name, "nt:unstructured");
        finalNodes.add(newRoot);
        newRoot.setProperty("sling:resourceType", GRANITEUI_MULTIFIELD_RT);
        // set properties
        copyProperty(root, "fieldLabel", newRoot, "fieldLabel");
        copyProperty(root, "fieldDescription", newRoot, "fieldDescription");

        Node field;
        if (root.hasNode("fieldConfig")) {
            field = JcrUtil.copy(root.getNode("fieldConfig"), newRoot, "field");
            field.setPrimaryType("cq:Widget");
            copyProperty(root, "name", field, "name");
        } else {
            field = newRoot.addNode("field", "nt:unstructured");
            finalNodes.add(field);
            field.setProperty("sling:resourceType", GRANITEUI_TEXTFIELD_RT);
        }

        // remove old root and return new root
        root.remove();
        return newRoot;
    }

}
