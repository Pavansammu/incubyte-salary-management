package com.acme.salary.seed;

import com.acme.salary.employee.Country;

import java.util.List;
import java.util.Random;

/**
 * Region-appropriate first and last names, so seeded data looks believable in a demo.
 * Pools are large enough (~1,500+ combinations per region) that a sorted list doesn't show
 * the same name over and over; some repeats remain, as in any real 10,000-person company.
 */
final class NamePool {

    private record Names(List<String> first, List<String> last) {
    }

    private static final Names INDIAN = new Names(
            List.of("Aarav", "Vivaan", "Aditya", "Arjun", "Sai", "Rohan", "Karthik", "Pavan", "Rahul", "Vikram",
                    "Ishaan", "Krishna", "Siddharth", "Nikhil", "Varun", "Abhishek", "Harsha", "Manoj", "Suresh", "Ajay",
                    "Ravi", "Deepak", "Gautam", "Kunal", "Naveen", "Ananya", "Diya", "Priya", "Sneha", "Kavya",
                    "Lakshmi", "Meera", "Pooja", "Divya", "Asha", "Ishita", "Neha", "Swathi", "Anjali", "Bhavana",
                    "Keerthi", "Shruti", "Tanvi", "Harini", "Radhika", "Sanjana", "Pallavi", "Nandini", "Ritu", "Sowmya"),
            List.of("Sharma", "Reddy", "Patel", "Rao", "Iyer", "Nair", "Kumar", "Gupta", "Singh", "Verma",
                    "Naidu", "Menon", "Joshi", "Mehta", "Chowdary", "Pillai", "Das", "Bhat", "Kulkarni", "Shah",
                    "Agarwal", "Banerjee", "Chatterjee", "Desai", "Goud", "Hegde", "Jain", "Kapoor", "Malhotra", "Mishra",
                    "Pandey", "Prasad", "Rajan", "Saxena", "Shetty", "Sinha", "Srinivasan", "Thakur", "Varma", "Yadav"));

    private static final Names JAPANESE = new Names(
            List.of("Haruto", "Ren", "Sota", "Yuto", "Kaito", "Riku", "Hiroshi", "Takumi", "Kenji", "Daiki",
                    "Yui", "Hina", "Aoi", "Sakura", "Mei", "Yuna", "Rin", "Akari", "Emi", "Nanami"),
            List.of("Sato", "Suzuki", "Takahashi", "Tanaka", "Watanabe", "Ito", "Yamamoto", "Nakamura", "Kobayashi", "Kato",
                    "Yoshida", "Yamada", "Sasaki", "Yamaguchi", "Matsumoto", "Inoue", "Kimura", "Hayashi", "Shimizu", "Mori"));

    private static final Names GERMAN = new Names(
            List.of("Lukas", "Leon", "Finn", "Jonas", "Paul", "Felix", "Maximilian", "Elias", "Noah", "Tim",
                    "Emma", "Mia", "Hannah", "Lea", "Sophie", "Lena", "Laura", "Anna", "Marie", "Clara"),
            List.of("Muller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Hoffmann", "Koch",
                    "Schulz", "Bauer", "Richter", "Klein", "Wolf", "Neumann", "Schwarz", "Zimmermann", "Braun", "Krause"));

    private static final Names SINGAPOREAN = new Names(
            List.of("Wei Jie", "Jun Hao", "Hui Min", "Xin Yi", "Ethan", "Chloe", "Ryan", "Isabelle", "Marcus", "Grace",
                    "Zhi Hao", "Jia Hui", "Darren", "Rachel", "Aloysius", "Vanessa", "Nurul", "Farhan", "Priya", "Arun"),
            List.of("Tan", "Lim", "Lee", "Ng", "Ong", "Wong", "Goh", "Chua", "Chan", "Teo",
                    "Koh", "Ang", "Yeo", "Tay", "Ho", "Low", "Sim", "Toh", "Rahman", "Pillai"));

    private static final Names ENGLISH = new Names(
            List.of("James", "Oliver", "William", "Noah", "Liam", "Jack", "Henry", "Lucas", "Daniel", "Michael",
                    "Ethan", "Benjamin", "Samuel", "Joseph", "David", "Matthew", "Andrew", "Ryan", "Nathan", "Thomas",
                    "Olivia", "Emma", "Charlotte", "Amelia", "Sophia", "Isla", "Ava", "Grace", "Emily", "Chloe",
                    "Mia", "Lily", "Hannah", "Zoe", "Ella", "Lucy", "Ruby", "Sarah", "Jessica", "Megan"),
            List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Taylor", "Wilson", "Davies", "Evans", "Thomas",
                    "Martin", "Clarke", "Walker", "Wright", "Robinson", "Thompson", "White", "Hughes", "Green", "Hall",
                    "Anderson", "Baker", "Campbell", "Carter", "Collins", "Cooper", "Edwards", "Harris", "Jackson", "King",
                    "Lewis", "Miller", "Mitchell", "Morgan", "Murphy", "Parker", "Roberts", "Scott", "Turner", "Young"));

    private NamePool() {
    }

    static String fullName(Country country, Random random) {
        Names names = switch (country) {
            case INDIA -> INDIAN;
            case JAPAN -> JAPANESE;
            case GERMANY -> GERMAN;
            case SINGAPORE -> SINGAPOREAN;
            case UNITED_STATES, UNITED_KINGDOM, CANADA, AUSTRALIA -> ENGLISH;
        };
        return pick(names.first(), random) + " " + pick(names.last(), random);
    }

    private static String pick(List<String> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }
}
