package com.ilucic.alfrescoplatformutil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Utility for users and groups.
 * 
 * @author Ivan Lucic
 *
 */
@Service("platformUtil.authorityUtil")
public class AuthorityUtil {

	@Autowired
	@Qualifier("AuthorityService")
	private AuthorityService authorityService;
	@Autowired
	@Qualifier("PersonService")
	private PersonService personService;
	@Autowired
	@Qualifier("platformUtil.nodeUtil")
	private NodeUtil nodeUtil;
	
	public static final QName PROP_MIDDLE_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "middleName");
	
	public static final String FORMAT_FULL_NAME_DEFAULT_WITH_MIDDLE_NAME = "{firstName} {middleName} {lastName}";
	public static final String FORMAT_FULL_NAME_DEFAULT_WITHOUT_MIDDLE_NAME = "{firstName} {lastName}";
	public static final String FORMAT_FULL_NAME_WITH_COMMA = "{lastName}, {firstName}";

	public static final String PLACEHOLDER_FIRST_NAME = "{firstName}";
	public static final String PLACEHOLDER_LAST_NAME = "{lastName}";
	public static final String PLACEHOLDER_MIDDLE_NAME = "{middleName}";
	
	/**
	 * Generates user full name with the following format: <strong>firstName middleName lastName</strong>.
	 * 
	 * @param userName
	 * @param includeMiddleName if <code>false</code>, will not include middle name in resulting string
	 * @return
	 */
	public String getUserFullName(String userName, boolean includeMiddleName) {
		NodeRef person = personService.getPersonOrNull(userName);

		return getUserFullName(person, includeMiddleName ? FORMAT_FULL_NAME_DEFAULT_WITH_MIDDLE_NAME : FORMAT_FULL_NAME_DEFAULT_WITHOUT_MIDDLE_NAME);
	}
	
	/**
	 * Generates user full name, using the provided formatter.
	 * 
	 * Allowed placeholder values are:
	 * <ul>
	 * <li>{firstName}</li>
	 * <li>{middleName}</li>
	 * <li>{lastName}</li>
	 * </ul>
	 *
	 * Example method call:<br>
	 * <code>getUserFullName("fcoppola", "{lastName}, {firstName} {middleName}")</code>
	 * <br>
	 * will return "Coppola, Francis Ford"
	 * <br/>
	 * See predefined formats in class constants.
	 * 
	 * @param userName
	 * @param format
	 * @return
	 */
	public String getUserFullName(String userName, String format) {
		NodeRef person = personService.getPersonOrNull(userName);

		return getUserFullName(person, format);
	}
	
	/**
	 * Generates user full name, using the provided formatter.
	 * 
	 * Allowed placeholder values are:
	 * <ul>
	 * <li>{firstName}</li>
	 * <li>{middleName}</li>
	 * <li>{lastName}</li>
	 * </ul>
	 *
	 * Example method call:<br>
	 * <code>getUserFullName({@link NodeRef} of user, "{lastName}, {firstName} {middleName}")</code>
	 * <br>
	 * will return "Coppola, Francis Ford"
	 * <br/>
	 * See predefined formats in class constants.
	 * 
	 * @param person
	 * @param format
	 * @return
	 */
	public String getUserFullName(NodeRef person, String format) {
		if (person == null) {
			return StringUtils.EMPTY;
		}

		String firstName = nodeUtil.getStringProperty(person, ContentModel.PROP_FIRSTNAME, StringUtils.EMPTY);
		String lastName = nodeUtil.getStringProperty(person, ContentModel.PROP_LASTNAME, StringUtils.EMPTY);
		String middleName = nodeUtil.getStringProperty(person, PROP_MIDDLE_NAME, StringUtils.EMPTY);

		return StringUtils.replaceEach(format,
				new String[] { PLACEHOLDER_FIRST_NAME, PLACEHOLDER_LAST_NAME, PLACEHOLDER_MIDDLE_NAME },
				new String[] { firstName, lastName, middleName });
	}
	
	/**
	 * Returns a list of e-mails from all users within the provided groups.
	 * 
	 * @param groupDisplayNames
	 * @return
	 */
	public List<String> extractUserEmailsFromGroups(List<String> groupDisplayNames) {
		return extractUserPropFromGroups(groupDisplayNames, ContentModel.PROP_EMAIL);
	}
	
	/**
	 * Returns a list of user names from all users within the provided groups.
	 * 
	 * @param groupDisplayNames
	 * @return
	 */
	public List<String> extractUsernamesFromGroups(List<String> groupDisplayNames) {
		return extractUserPropFromGroups(groupDisplayNames, ContentModel.PROP_USERNAME);
	}
	
	/**
	 * Returns a list of user full names from all users within the provided
	 * groups. Full names are formatted based on the passed formatter.
	 * 
	 * Allowed placeholder values are:
	 * <ul>
	 * <li>{firstName}</li>
	 * <li>{middleName}</li>
	 * <li>{lastName}</li>
	 * </ul>
	 *
	 * Example format:<br>
	 * <i>"{lastName}, {firstName} {middleName}"</i> <br>
	 * will return "Coppola, Francis Ford"
	 * <br/>
	 * See predefined formats in class constants.
	 * 
	 * @param groupDisplayNames
	 * @param format
	 * @return
	 */
	public List<String> extractUserFullNamesFormGroups(List<String> groupDisplayNames, String format) {
		return groupDisplayNames.stream().map(groupDisplayName -> {
			return authorityService.findAuthorities(AuthorityType.GROUP, null, false, groupDisplayName, null);
		}).filter(group -> group.size() == 1).map(group -> group.iterator().next()).map(group -> {
			return authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
		}).flatMap(Set::stream).map(userName -> {
			NodeRef user = personService.getPersonOrNull(userName);
			return getUserFullName(user, format);
		}).collect(Collectors.toList());
	}
	
	/**
	 * Extracts provided user property from the users within provided groups.
	 * 
	 * @param groupDisplayNames
	 * @param prop
	 * @return
	 */
	protected List<String> extractUserPropFromGroups(List<String> groupDisplayNames, QName prop) {
		return groupDisplayNames.stream().map(groupDisplayName -> {
			return authorityService.findAuthorities(AuthorityType.GROUP, null, false, groupDisplayName, null);
		}).filter(group -> group.size() == 1).map(group -> group.iterator().next()).map(group -> {
			return authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
		}).flatMap(Set::stream).map(userName -> {
			NodeRef user = personService.getPerson(userName);
			return nodeUtil.getStringProperty(user, prop);
		}).collect(Collectors.toList());
	}
}
