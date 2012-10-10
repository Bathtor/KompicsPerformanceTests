package se.kth.ii2202.experiments.nnar;

import java.util.Random;
import se.kth.ii2202.experiments.common.FinishTimeout;
import se.kth.ii2202.experiments.common.MeasureTimeout;
import se.kth.ii2202.experiments.common.WarmUpTimeout;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author carbone
 */
public class Main extends ComponentDefinition {

    Positive<NNAR> nnar = requires(NNAR.class);
    Positive<Timer> timer = requires(Timer.class);
    
    long preloadTime = 1000l;
    ScheduleTimeout preloadTimeout, warmUpTimeout, measureTimeout;
    long warmUpTime = 0l;
    long measureTime = 0l;
    //String cmd = "";
    long startTime = System.nanoTime();
    long stageEnd, reqStart;
    boolean warmUp = false;
    Random rand = new Random();
    int selfId = -1;

    public Main() {
        subscribe(onInit, control);
        subscribe(onWarmupTimeout, timer);
        subscribe(onMeasureTimeout, timer);
        subscribe(onFinishTimeout, timer);
        subscribe(onReadRet, nnar);
        subscribe(onWriteRet, nnar);
    }
    Handler<NnarMainInit> onInit = new Handler<NnarMainInit>() {
        @Override
        public void handle(NnarMainInit e) {
        	selfId = e.getTopology().getSelfId();
            
            System.out.println("Starting NNAR Main " + selfId);
            
            warmUpTime = e.getScenario().getWarmUpTime();
            measureTime = e.getScenario().getMeasureTime();
            //cmd = e.getScenario().getCommand(e.getTopology().getSelfId());
            preloadTimeout = new ScheduleTimeout(preloadTime);
            preloadTimeout.setTimeoutEvent(new WarmUpTimeout(preloadTimeout));
            warmUpTimeout = new ScheduleTimeout(warmUpTime);
            warmUpTimeout.setTimeoutEvent(new MeasureTimeout(warmUpTimeout));
            measureTimeout = new ScheduleTimeout(measureTime);
            measureTimeout.setTimeoutEvent(new FinishTimeout(measureTimeout));
            trigger(preloadTimeout, timer);
        }
    };
    Handler<WarmUpTimeout> onWarmupTimeout = new Handler<WarmUpTimeout>() {
        @Override
        public void handle(WarmUpTimeout event) {
            System.out.println("Node " + selfId + " starting Warmup Phase");
            stageEnd = System.nanoTime() + warmUpTime * 1000 * 1000;
            warmUp = true;
            trigger(warmUpTimeout, timer);
            startOp();
        }
    };
    Handler<MeasureTimeout> onMeasureTimeout = new Handler<MeasureTimeout>() {
        @Override
        public void handle(MeasureTimeout event) {
            System.out.println("Node " + selfId + " starting Measure Phase");
            stageEnd = System.nanoTime() + measureTime * 1000 * 1000;
            warmUp = false;
            trigger(measureTimeout, timer);
            startOp();
        }
    };
    Handler<FinishTimeout> onFinishTimeout = new Handler<FinishTimeout>() {
        @Override
        public void handle(FinishTimeout event) {
            System.out.println("Node " + selfId + " finished Measure Phase");
        }
    };
    Handler<NNARreadreturn> onReadRet = new Handler<NNARreadreturn>() {
        @Override
        public void handle(NNARreadreturn event) {
            timeOp("READ");
            startOp();
        }
    };
    Handler<NNARwritereturn> onWriteRet = new Handler<NNARwritereturn>() {
        @Override
        public void handle(NNARwritereturn event) {
            timeOp("WRITE");
            startOp();
        }
    };

    private void startOp() {
        if (System.nanoTime() < stageEnd) {
            boolean read = rand.nextBoolean();
            if (read) {
                if (!warmUp) {
                    reqStart = System.nanoTime();
                }
                //System.out.println("Starting READ op");
                trigger(new NNARread(), nnar);
            } else {
                byte[] valbytes = new byte[16];
                rand.nextBytes(valbytes);
                String value = new String(valbytes);
                if (!warmUp) {
                    reqStart = System.nanoTime();
                }
                //System.out.println("Starting WRITE op");
                trigger(new NNARwrite(value), nnar);
            }
        }
    }

    private void timeOp(String opType) {
    	//System.out.println("Finished "+opType+" op");
        if (!warmUp) {
            long endTime = System.nanoTime();
            long diff = endTime - reqStart;
            long startDiff = endTime - startTime;
            //System.err.println(startDiff + " " + opType + " completed in " + diff + "ns");
            Application.log(startDiff, opType, diff);
        }
    }
}
