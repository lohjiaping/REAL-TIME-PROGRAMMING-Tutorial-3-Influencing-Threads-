package Week_05;

    public class InfluencingThreads {

        public static void main(String[] args) {
            Runway runway = new Runway();

            Airplane a1 = new Airplane("MAS01 EL-MARIACHI", FlightPriority.NORMAL, runway);
            Airplane a2 = new Airplane("MAS02 EUDORA", FlightPriority.VIP, runway);
            Airplane a3 = new Airplane("MAS03 DEJAVU", FlightPriority.LOW, runway);
            Airplane a4 = new Airplane("MAS04 UTOPIA", FlightPriority.EMERGENCY, runway);

            Thread t1 = new Thread(a1, "MAS01-EL-MARIACHI");
            Thread t2 = new Thread(a2, "MAS02-EUDORA");
            Thread t3 = new Thread(a3, "MAS03-DEJAVU");
            Thread t4 = new Thread(a4, "MAS04-UTOPIA");

            t1.setPriority(mapPriority(a1.getPriority()));
            t2.setPriority(mapPriority(a2.getPriority()));
            t3.setPriority(mapPriority(a3.getPriority()));
            t4.setPriority(mapPriority(a4.getPriority()));

            System.out.println("Control Tower: Starting flight threads and assigning priorities.");
            System.out.printf("%s priority=%d\n", t1.getName(), t1.getPriority());
            System.out.printf("%s priority=%d\n", t2.getName(), t2.getPriority());
            System.out.printf("%s priority=%d\n", t3.getName(), t3.getPriority());
            System.out.printf("%s priority=%d\n", t4.getName(), t4.getPriority());

            t1.start();
            t2.start();
            t3.start();
            t4.start();

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("\nControl Tower: Interrupting Flight MAS01 EL-MARIACHI due to heavy rain.");
            t1.interrupt();

            try {
                t1.join();
                t2.join();
                t3.join();
                t4.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Control Tower was interrupted while waiting for flights.");
            }

            System.out.println("\nAll flights completed. Control Tower closing operations.");
        }

        private static int mapPriority(FlightPriority p) {
            switch (p) {
                case VIP:
                case EMERGENCY:
                    return Thread.MAX_PRIORITY;
                case NORMAL:
                    return Thread.NORM_PRIORITY;
                case LOW:
                default:
                    return Thread.MIN_PRIORITY;
            }
        }
    }

    enum FlightPriority {
        VIP, EMERGENCY, NORMAL, LOW
    }

    class Runway {

        public synchronized void useRunway(Airplane airplane) {
            String name = airplane.getName();
            Thread current = Thread.currentThread();

            System.out.printf("%s: Taking off (thread: %s, priority: %d)\n",
                    name, current.getName(), current.getPriority());

            try {
                for (int step = 1; step <= 5; step++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    System.out.printf("%s: on runway - step %d/5\n", name, step);
                    Thread.sleep(700);
                }

                System.out.printf("%s: Leaving the runway (successful takeoff).\n", name);

            } catch (InterruptedException ex) {
                System.out.printf("%s: INTERRUPTED during takeoff! Aborting and clearing runway.\n", name);
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("Control Tower: Runway is now free.");
            }
        }
    }

    class Airplane implements Runnable {
        private final String name;
        private final FlightPriority priority;
        private final Runway runway;

        public Airplane(String name, FlightPriority priority, Runway runway) {
            this.name = name;
            this.priority = priority;
            this.runway = runway;
        }

        public String getName() {
            return name;
        }

        public FlightPriority getPriority() {
            return priority;
        }

        @Override
        public void run() {
            Thread current = Thread.currentThread();
            try {
                System.out.printf("%s: Requesting runway access (thread: %s, priority: %d)\n",
                        name, current.getName(), current.getPriority());

                if (Thread.currentThread().isInterrupted()) {
                    System.out.printf("%s: Was interrupted before takeoff - aborting.\n", name);
                    return;
                }

                runway.useRunway(this);

            } catch (Exception ex) {
                System.out.printf("%s: Exception in flight thread: %s\n", name, ex.getMessage());
            } finally {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.printf("%s: Exiting due to interruption. Please reschedule.\n", name);
                }
            }
        }
    }