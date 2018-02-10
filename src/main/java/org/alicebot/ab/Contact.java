package org.alicebot.ab;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is here to simulate a Contacts database for the purpose of testing contactaction.aiml
 */
public class Contact {

    private static AtomicLong contactCount = new AtomicLong();
    private static Map<String, Contact> idContactMap = new ConcurrentHashMap<>();
    private static Map<String, String> nameIdMap = new ConcurrentHashMap<>();

    private String contactId;
    private String displayName;
    private String birthday;
    private Map<String, String> phones;
    private Map<String, String> emails;

    public static String multipleIds(String contactName) {
        String patternString = " (" + contactName.toUpperCase() + ") ";
        while (patternString.contains(" ")) {
            patternString = patternString.replace(" ", "(.*)");
        }
        Pattern pattern = Pattern.compile(patternString);
        Set<String> keys = nameIdMap.keySet();
        StringBuilder result = new StringBuilder();
        int idCount = 0;
        for (String key : keys) {
            Matcher m = pattern.matcher(key);
            if (m.find()) {
                result.append(nameIdMap.get(key.toUpperCase())).append(" ");
                idCount++;
            }
        }
        if (idCount <= 1) {
            return "false";
        }
        return result.toString().trim();
    }

    public static String contactId(String contactName) {
        String patternString = " " + contactName.toUpperCase() + " ";
        while (patternString.contains(" ")) {
            patternString = patternString.replace(" ", ".*");
        }
        Pattern pattern = Pattern.compile(patternString);
        Set<String> keys = nameIdMap.keySet();
        for (String key : keys) {
            Matcher m = pattern.matcher(key);
            if (m.find()) {
                return nameIdMap.get(key.toUpperCase()).trim();
            }
        }
        return "unknown";
    }

    public static String displayName(String id) {
        Contact c = idContactMap.get(id.toUpperCase());
        String result = "unknown";
        if (c != null) {
            result = c.displayName;
        }
        return result;
    }

    public static String dialNumber(String type, String id) {
        String result = "unknown";
        Contact c = idContactMap.get(id.toUpperCase());
        if (c != null) {
            String dialNumber = c.phones.get(type.toUpperCase());
            if (dialNumber != null) {
                result = dialNumber;
            }
        }
        return result;
    }

    public static String emailAddress(String type, String id) {
        String result = "unknown";
        Contact c = idContactMap.get(id.toUpperCase());
        if (c != null) {
            String emailAddress = c.emails.get(type.toUpperCase());
            if (emailAddress != null) {
                result = emailAddress;
            }
        }
        return result;
    }

    public static String birthday(String id) {
        Contact c = idContactMap.get(id.toUpperCase());
        if (c == null) return "unknown";
        else return c.birthday;
    }

    public Contact(String displayName, String phoneType, String dialNumber, String emailType, String emailAddress, String birthday) {
        contactId = "ID" + contactCount.incrementAndGet();
        phones = new HashMap<>();
        emails = new HashMap<>();
        idContactMap.put(contactId.toUpperCase(), this);
        addPhone(phoneType, dialNumber);
        addEmail(emailType, emailAddress);
        addName(displayName);
        addBirthday(birthday);
    }

    public void addPhone(String type, String dialNumber) {
        phones.put(type.toUpperCase(), dialNumber);
    }

    public void addEmail(String type, String emailAddress) {
        emails.put(type.toUpperCase(), emailAddress);
    }

    public void addName(String name) {
        displayName = name;
        nameIdMap.put(displayName.toUpperCase(), contactId);
    }

    public void addBirthday(String birthday) {
        this.birthday = birthday;
    }
}
