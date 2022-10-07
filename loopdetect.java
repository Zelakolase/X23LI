import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
/**
Usage: java loopdetect UpperBoundaryInclusive MaxValueForn
Example: java loopdetect 1000 13
**/
public class loopdetect {
    volatile static AtomicBigInteger atomicInt = new AtomicBigInteger(BigInteger.ZERO);
    static int ENUMERATION_CONST;
    static BigInteger two = new BigInteger("2");
    static BigInteger SIZE;
    static BigDecimal SIZEBD;
    static BigInteger HUNDRED = new BigInteger("100");
    static BigDecimal HH = new BigDecimal(HUNDRED);
    public static void main(String[] args) throws Exception {
        SIZE = new BigInteger(args[0]);
        SIZEBD = new BigDecimal(SIZE);
        ENUMERATION_CONST = Integer.parseInt(args[1]);

        ExecutorService ES = Executors.newCachedThreadPool();
        long F = System.nanoTime();
        for(BigInteger i = BigInteger.ONE;i.compareTo(SIZE)<=0;i = i.add(two)) {
            Worker w = new Worker(i);
            ES.execute(w);
        }
        atAS(ES);
        long L = (System.nanoTime() - F)/1_000_000; // sec
        System.out.print("Final Series coverage is ");
        printPercent(atomicInt.get());
        System.out.println("Speed is "+SIZEBD.divide(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(ENUMERATION_CONST)).divide(new BigDecimal(String.valueOf(L)), MathContext.DECIMAL128)+" ops/sec");
    }

    public static class Worker implements Runnable {
        BigInteger is;
        Worker(BigInteger i) {
            this.is = i;
        }
        @Override
        public void run() {
            for(int o = 0;o <= ENUMERATION_CONST;o++) {
                if(is.multiply(BigInteger.ONE.shiftLeft(o)).compareTo(SIZE) <= 0) {
                    atomicInt.getAndIncrement();
                }
            }
           // printPercent(atomicInt.get());
        }
    }

    public static void printPercent(BigInteger integer) {
        BigDecimal BD0 = new BigDecimal(integer);
        System.out.println(new StringBuilder(BD0.divide(SIZEBD, MathContext.DECIMAL128).multiply(HH).toString()).append("%"));
    }

    public static void atAS(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public static final class AtomicBigInteger {

        private final AtomicReference<BigInteger> bigInteger;
      
        public AtomicBigInteger(final BigInteger bigInteger) {
          this.bigInteger = new AtomicReference<>(Objects.requireNonNull(bigInteger));
        }
      
        public BigInteger getAndIncrement() {
          return bigInteger.getAndAccumulate(BigInteger.ONE, (previous, x) -> previous.add(x));
        }
      
        public BigInteger get() {
          return bigInteger.get();
        }
      }
}
