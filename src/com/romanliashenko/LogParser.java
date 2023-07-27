package com.romanliashenko;

import com.romanliashenko.query.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery, QLQuery {
    List<LogEntry> entryList = new ArrayList<LogEntry>();
    Path logDir;

    public LogParser(Path logDir){
        this.logDir = logDir;

        try {
            entryList = readLogFile();
        } catch (Exception e) {

        }
    }

    @Override
    public Set<String> getAllUsers() {
        return entryList.stream()
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .map(x -> x.user)
                .collect(Collectors.toSet())
                .size();
    }

    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .map(x -> x.event)
                .collect(Collectors.toSet())
                .size();
    }

    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.ip.equals(ip))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.LOGIN))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.DOWNLOAD_PLUGIN))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.WRITE_MESSAGE))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.SOLVE_TASK))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.SOLVE_TASK))
                .filter(x -> x.taskNumber == task)
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.DONE_TASK))
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.DONE_TASK))
                .filter(x -> x.taskNumber == task)
                .map(x -> x.user)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(event))
                .map(x -> x.date)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.status.equals(Status.FAILED))
                .map(x -> x.date)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.status.equals(Status.ERROR))
                .map(x -> x.date)
                .collect(Collectors.toSet());
    }

    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {
        Set<Date> set = streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(Event.LOGIN))
                .map(x -> x.date)
                .collect(Collectors.toSet());
        long currentMinDate = Long.MAX_VALUE;
        Date seekedDate = null;
        if (set.size() != 0) {
            for (Date date : set)
                if (date.getTime() < currentMinDate) {
                    currentMinDate = date.getTime();
                    seekedDate = date;
                }
        }
        return seekedDate;
    }

    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        Set<Date> set = streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(Event.SOLVE_TASK))
                .filter(x -> x.taskNumber == task)
                .map(x -> x.date)
                .collect(Collectors.toSet());
        long currentMinDate = Long.MAX_VALUE;
        Date seekedDate = null;
        if (set.size() != 0) {
            for (Date date : set)
                if (date.getTime() < currentMinDate) {
                    currentMinDate = date.getTime();
                    seekedDate = date;
                }
        }
        return seekedDate;
    }

    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        Set<Date> set = streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(Event.DONE_TASK))
                .filter(x -> x.taskNumber == task)
                .map(x -> x.date)
                .collect(Collectors.toSet());
        long currentMinDate = Long.MAX_VALUE;
        Date seekedDate = null;
        if (set.size() != 0) {
            for (Date date : set)
                if (date.getTime() < currentMinDate) {
                    currentMinDate = date.getTime();
                    seekedDate = date;
                }
        }
        return seekedDate;
    }

    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(Event.WRITE_MESSAGE))
                .map(x -> x.date)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .filter(x -> x.event.equals(Event.DOWNLOAD_PLUGIN))
                .map(x -> x.date)
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfAllEvents(Date after, Date before) {
        return getAllEvents(after, before).size();
    }

    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .map(x -> x.event)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.ip.equals(ip))
                .map(x -> x.event)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .map(x -> x.event)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.status.equals(Status.FAILED))
                .map(x -> x.event)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.status.equals(Status.ERROR))
                .map(x -> x.event)
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfAttemptToSolveTask(int task, Date after, Date before) {
        return (int) streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.SOLVE_TASK))
                .filter(x -> x.taskNumber.equals(task))
                .map(x -> x.event)
                .count();
    }

    @Override
    public int getNumberOfSuccessfulAttemptToSolveTask(int task, Date after, Date before) {
        return (int) streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.DONE_TASK))
                .filter(x -> x.taskNumber == task)
                .map(x -> x.event)
                .count();
    }

    @Override
    public Map<Integer, Integer> getAllSolvedTasksAndTheirNumber(Date after, Date before) {
        List<Integer> keySet = streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.SOLVE_TASK))
                .map(x -> x.taskNumber)
                .collect(Collectors.toList());

        Map<Integer, Integer> map = new HashMap<>();
        for (Integer i : keySet) {
            if (!map.containsKey(i))
                map.put(i, 1);
            else
                map.replace(i, map.get(i) + 1);
        }
        return map;
    }

    @Override
    public Map<Integer, Integer> getAllDoneTasksAndTheirNumber(Date after, Date before) {
        List<Integer> keySet = streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(Event.DONE_TASK))
                .map(x -> x.taskNumber)
                .collect(Collectors.toList());

        Map<Integer, Integer> map = new HashMap<>();
        for (Integer i : keySet) {
            if (!map.containsKey(i))
                map.put(i, 1);
            else
                map.replace(i, map.get(i) + 1);
        }
        return map;
    }

    @Override
    public Set<Object> execute(String query) throws IOException, ParseException {
        Set<Object> set = null;
        switch (query) {
            case "get ip":
                set = new HashSet<>(getUniqueIPs(null, null));
                break;
            case  "get user":
                set = new HashSet<>(getAllUsers());
                break;
            case "get date":
                set = streamInTheInterval(entryList.stream(), null, null)
                        .map(x -> x.date).collect(Collectors.toSet());
                break;
            case "get event":
                set = new HashSet<>(getAllEvents(null, null));
                break;
            case "get status":
                set = streamInTheInterval(entryList.stream(), null, null)
                        .map(x -> x.status)
                        .collect(Collectors.toSet());
                break;
        }

        List<String> list = Arrays.stream(query.split("\\s|=")).collect(Collectors.toList());
        if (set == null && !list.contains("between") && !list.contains("and")){
            set = new HashSet<>();
            ArrayList<String> strings = new ArrayList<>(list);
            for (int i = 0; i < strings.size(); i++) {
                if (strings.get(i).startsWith("\"")) {
                    StringBuilder resultValue = new StringBuilder();
                    for (int j = i; j <= strings.size() - 1; j++)
                        resultValue.append(strings.get(j)).append(" ");

                    resultValue.deleteCharAt(resultValue.indexOf("\""));
                    resultValue.deleteCharAt(resultValue.lastIndexOf(" "));
                    resultValue.deleteCharAt(resultValue.lastIndexOf("\""));
                    strings.subList(i - 1, strings.size() - 1).clear();
                    strings.removeIf(s -> s.equals("") || s.contains("\""));
                    strings.trimToSize();
                    strings.add(resultValue.toString());
                    break;
                }
            }


            String field1 = strings.get(1);
            String field2 = strings.get(3);
            String value = strings.get(4);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            if (strings.get(0).equals("get") && strings.get(2).equals("for") ){
                for (LogEntry logEntry : entryList) {
                    if (field2.equals("ip") && logEntry.ip.equals(value)
                    || field2.equals("user") && logEntry.user.contains(value)
                    || field2.equals("date") && logEntry.date.getTime() == simpleDateFormat.parse(value).getTime()
                    || field2.equals("event") && logEntry.event.equals(Event.valueOf(value))
                    || field2.equals("status") && logEntry.status.equals(Status.valueOf(value))){
                        switch (field1) {
                            case "ip":
                                set.add(logEntry.ip);
                                break;
                            case "user":
                                set.add(logEntry.user);
                                break;
                            case "date":
                                set.add(logEntry.date);
                                break;
                            case "event":
                                set.add(logEntry.event);
                                break;
                            case "status":
                                set.add(logEntry.status);
                                break;
                        }
                    }
                }
            }
        }
        else {
            set = new HashSet<>();
            Pattern pattern = pattern = Pattern.compile("get (ip|user|date|event|status)"
                    + "( for (ip|user|date|event|status) = \"(.*?)\")?"
                    + "( and date between \"(.*?)\" and \"(.*?)\")?");
            Matcher matcher = pattern.matcher(query);
            matcher.find();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

            String field1 = null;
            String field2 = null;
            String value1 = null;
            Date after = null;
            Date before = null;

            field1 = matcher.group(1);
            if (matcher.group(2) != null) {
                field2 = matcher.group(3);
                value1 = matcher.group(4);
                if (matcher.group(5) != null) {
                    try {
                        after = simpleDateFormat.parse(matcher.group(6));
                        before = simpleDateFormat.parse(matcher.group(7));
                    } catch (ParseException e) {
                    }
                }
            }
            for (LogEntry logEntry : entryList) {
                if (isDateInTheInterval(logEntry.date, after, before)){
                    if (field2.equals("ip") && logEntry.ip.equals(value1)
                            || field2.equals("user") && logEntry.user.contains(value1)
                            || field2.equals("date") && logEntry.date.getTime() == simpleDateFormat.parse(value1).getTime()
                            || field2.equals("event") && logEntry.event.equals(Event.valueOf(value1))
                            || field2.equals("status") && logEntry.status.equals(Status.valueOf(value1))) {
                        switch (field1) {
                            case "ip":
                                set.add(logEntry.ip);
                                break;
                            case "user":
                                set.add(logEntry.user);
                                break;
                            case "date":
                                set.add(logEntry.date);
                                break;
                            case "event":
                                set.add(logEntry.event);
                                break;
                            case "status":
                                set.add(logEntry.status);
                                break;
                        }
                    }
                }
            }

        }
        return set;
    }

    private class LogEntry {
        String ip;
        String user;
        Date date;
        Event event;
        Integer taskNumber;
        Status status;

        public LogEntry(String ip, String user, Date date, Event event, Integer taskNumber, Status status) {
            this.ip = ip;
            this.user = user;
            this.date = date;
            this.event = event;
            this.taskNumber = taskNumber;
            this.status = status;
        }

        @Override
        public String toString() {
            return "LogEntry{" +
                    "ip='" + ip + '\'' +
                    ", user='" + user + '\'' +
                    ", date=" + date +
                    ", event=" + event +
                    ", taskNumber=" + taskNumber +
                    ", status=" + status +
                    '}';
        }
    }

    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {
        return (int) getUniqueIPs(after, before).stream().count();
    }

    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .map(x -> x.ip)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.user.equals(user))
                .map(x -> x.ip)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.event.equals(event))
                .map(x -> x.ip)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        return streamInTheInterval(entryList.stream(), after, before)
                .filter(x -> x.status.equals(status))
                .map(x -> x.ip)
                .collect(Collectors.toSet());
    }

    private List<LogEntry> readLogFile() throws IOException, ParseException {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.logDir, "*.log");
        ArrayList<String> logStrings = new ArrayList<>();
        for (Path path : directoryStream)
            logStrings.addAll(Files.readAllLines(path));

        List<LogEntry> logs = new ArrayList<>();
        for (String s : logStrings){
            String[] values = s.split("\\t");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            try {
                logs.add(new LogEntry(values[0],
                        values[1],
                        dateFormat.parse(values[2]),
                        Event.valueOf(values[3].split(" ")[0]),
                        Integer.parseInt(values[3].split(" ")[1]),
                        Status.valueOf(values[4])));
            } catch (Exception e) {
                logs.add(new LogEntry(values[0],
                        values[1],
                        dateFormat.parse(values[2]),
                        Event.valueOf(values[3].split(" ")[0]),
                        null,
                        Status.valueOf(values[4])));
            }
        }

        return logs;
    }

    private Stream<LogEntry> streamInTheInterval(Stream<LogEntry> str, Date after, Date before) {
        if (after == null && before == null)
            return str;
        else if (after == null)
            return str.filter(x -> x.date.getTime() <= before.getTime());
        else if (before == null)
            return str.filter(x -> x.date.getTime() >= after.getTime());
        else
            return str.filter(x -> x.date.getTime() >= after.getTime() && x.date.getTime() <= before.getTime());
    }

    private boolean isDateInTheInterval (Date current, Date after, Date before) {
        return (after == null && before == null)
                || (after == null && current.getTime() < before.getTime())
                || (current.getTime() > after.getTime() && before == null)
                || (current.getTime() > after.getTime() && current.getTime() < before.getTime());
    }


//    public boolean isDateInTheInterval (Date after, Date before, Date current) {
//        if (after == null)
//            return current.getTime() <= before.getTime();
//        else if (before == null)
//            return current.getTime() >= after.getTime();
//
//        return current.getTime() >= after.getTime() && current.getTime() <= before.getTime();
//    }
}