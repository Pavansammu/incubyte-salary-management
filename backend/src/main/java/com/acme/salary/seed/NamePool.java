package com.acme.salary.seed;

import com.acme.salary.employee.Country;

import java.util.List;
import java.util.Random;

/** Region-appropriate first and last names, so seeded data looks believable in a demo. */
final class NamePool {

    private record Names(List<String> first, List<String> last) {
    }

    private static final Names INDIAN = new Names(
            List.of("Aarav", "Vivaan", "Aditya", "Arjun", "Sai", "Rohan", "Karthik", "Pavan", "Rahul", "Vikram",
                    "Ananya", "Diya", "Priya", "Sneha", "Kavya", "Lakshmi", "Meera", "Pooja", "Divya", "Asha"),
            List.of("Sharma", "Reddy", "Patel", "Rao", "Iyer", "Nair", "Kumar", "Gupta", "Singh", "Verma",
                    "Naidu", "Menon", "Joshi", "Mehta", "Chowdary", "Pillai", "Das", "Bhat", "Kulkarni", "Shah"));

    private static final Names JAPANESE = new Names(
            List.of("Haruto", "Ren", "Sota", "Yuto", "Kaito", "Yui", "Hina", "Aoi", "Sakura", "Mei"),
            List.of("Sato", "Suzuki", "Takahashi", "Tanaka", "Watanabe", "Ito", "Yamamoto", "Nakamura", "Kobayashi", "Kato"));

    private static final Names GERMAN = new Names(
            List.of("Lukas", "Leon", "Finn", "Jonas", "Paul", "Emma", "Mia", "Hannah", "Lea", "Sophie"),
            List.of("Muller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Hoffmann", "Koch"));

    private static final Names SINGAPOREAN = new Names(
            List.of("Wei Jie", "Jun Hao", "Hui Min", "Xin Yi", "Ethan", "Chloe", "Ryan", "Isabelle", "Marcus", "Grace"),
            List.of("Tan", "Lim", "Lee", "Ng", "Ong", "Wong", "Goh", "Chua", "Chan", "Teo"));

    private static final Names ENGLISH = new Names(
            List.of("James", "Oliver", "William", "Noah", "Liam", "Jack", "Henry", "Lucas", "Daniel", "Michael",
                    "Olivia", "Emma", "Charlotte", "Amelia", "Sophia", "Isla", "Ava", "Grace", "Emily", "Chloe"),
            List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Taylor", "Wilson", "Davies", "Evans", "Thomas",
                    "Martin", "Clarke", "Walker", "Wright", "Robinson", "Thompson", "White", "Hughes", "Green", "Hall"));

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
