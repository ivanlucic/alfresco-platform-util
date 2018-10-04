package com.ilucic.alfrescoplatformutil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import com.google.common.collect.Lists;

/**
 * Node Service wrapper for Alfresco {@link NodeService}.
 * 
 * @author Ivan Lucic
 *
 */
public class NodeUtil {

	private NodeService nodeService;
	
	/**
	 * Returns node String property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public String getStringProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return (String) propValue;
	}

	/**
	 * Returns node String property value or default value if property
	 * value is null.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @param defaultValue value to be returned in the case that property is not set on the node
	 * @return
	 */
	public String getStringProperty(NodeRef nodeRef, QName qname, String defaultValue) {
		String propValue = getStringProperty(nodeRef, qname);

		return propValue != null ? propValue : defaultValue;
	}

	/**
	 * Returns node Date property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public Date getDateProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return (Date) propValue;
	}

	/**
	 * Returns node Date property value or default value if property
	 * value is null.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @param defaultValue defaultValue value to be returned in the case that property is not set on the node
	 * @return
	 */
	public Date getDateProperty(NodeRef nodeRef, QName qname, Date defaultValue) {
		Date propValue = getDateProperty(nodeRef, qname);

		return propValue != null ? propValue : defaultValue;
	}

	/**
	 * Returns node Boolean property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public boolean getBooleanProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return propValue != null && Boolean.valueOf(propValue.toString());
	}

	/**
	 * Returns node multivalued property value as a list of values of the provided type.
	 * If value is null, returns null or an empty list, based on the provided flag.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @param elementType the type of the multivalued property
	 * @param emptyListIfNull
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getMultivaluedProperty(NodeRef nodeRef, QName qname, Class<T> elementType, boolean emptyListIfNull) {
		Serializable propValue = getProperty(nodeRef, qname);

		if (propValue == null) {
			return emptyListIfNull ? Lists.newArrayList() : null;
		}

		return (List<T>) propValue;
	}

	/**
	 * Returns node Integer property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public Integer getIntegerProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return propValue != null ? (Integer) propValue : null;
	}

	/**
	 * Returns node Integer property value or default value if property
	 * value is null.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @param defaultValue defaultValue value to be returned in the case that property is not set on the node
	 * @return
	 */
	public Integer getIntegerProperty(NodeRef nodeRef, QName qname, int defaultValue) {
		Integer propValue = getIntegerProperty(nodeRef, qname);

		return propValue != null ? propValue : defaultValue;
	}

	/**
	 * Returns node {@link NodeRef} property value (property of type d:noderef).
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public NodeRef getNodeRefProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return (NodeRef) propValue;
	}

	/**
	 * Returns node property value of the specified type.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @param propType the type of the property
	 * @return
	 */
	public <T> T getProperty(NodeRef nodeRef, QName qname, Class<T> propType) {
		Serializable propValue = getProperty(nodeRef, qname);

		return propValue != null ? propType.cast(propValue) : null;
	}

	/**
	 * Returns all associated node refs for all target associations of a node.
	 * 
	 * @param nodeRef
	 * @param qnamePattern
	 * @return
	 */
	public List<NodeRef> getAssociatedTargetNodes(NodeRef nodeRef, QNamePattern qnamePattern) {
		return nodeService.getTargetAssocs(nodeRef, qnamePattern).stream().map(childAssocRef -> childAssocRef.getTargetRef()).collect(Collectors.toList());
	}

	/**
	 * Returns all associated node refs for all target associations of a
	 * node, filtered out by provided property value.
	 * 
	 * @param nodeRef
	 * @param propertyQName
	 * @param propertyValue
	 * @param qnamePattern
	 * @return
	 */
	public List<NodeRef> getAssociatedTargetNodesByPropertyValue(NodeRef nodeRef, QName propertyQName, Serializable propertyValue, QNamePattern qnamePattern) {
		return nodeService.getTargetAssocsByPropertyValue(nodeRef, qnamePattern, propertyQName, propertyValue).stream().map(childAssocRef -> childAssocRef.getTargetRef()).collect(Collectors.toList());
	}

	/**
	 * Returns source node of the provided target node association. Method should
	 * be used only for <strong>many=false</strong> source assocs, because it will
	 * only return one node (if many are found, it will return the first one).
	 * 
	 * @param nodeRef
	 * @param qnamePattern
	 * @return
	 */
	public NodeRef getAssociatedSourceNode(NodeRef nodeRef, QNamePattern qnamePattern) {
		List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(nodeRef, qnamePattern);

		return sourceAssocs.size() > 0 ? sourceAssocs.get(0).getSourceRef() : null;
	}

	/**
	 * Returns node Long property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	public Long getLongProperty(NodeRef nodeRef, QName qname) {
		Serializable propValue = getProperty(nodeRef, qname);

		return (Long) propValue;
	}

	/**
	 * Returns node property value.
	 * 
	 * @param nodeRef
	 * @param qname
	 * @return
	 */
	protected Serializable getProperty(NodeRef nodeRef, QName qname) {
		return nodeService.getProperty(nodeRef, qname);
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
