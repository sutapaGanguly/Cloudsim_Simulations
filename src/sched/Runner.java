package sched;
import java.util.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

public class Runner {
    public static class Result { public double simulated, avgCompletion, imbalance, schedMs; }

    public static Result run(Scheduler scheduler, long seed, int tasks, int vms) throws Exception {
        Log.disable();
        CloudSim.init(1, Calendar.getInstance(), false);
        createDatacenter("Datacenter_0");
        DatacenterBroker broker = new DatacenterBroker("Broker");
        int userId = broker.getId();
        int[] mipsOptions = {500, 1000, 1500, 2000};
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < vms; i++)
            vmList.add(new Vm(i, userId, mipsOptions[i % 4], 1, 512, 1000, 10000, "Xen",
                              new CloudletSchedulerSpaceShared()));
        broker.submitVmList(vmList);
        Random rnd = new Random(seed);
        List<Cloudlet> cloudlets = new ArrayList<>();
        UtilizationModel full = new UtilizationModelFull();
        for (int i = 0; i < tasks; i++) {
            long length = 1000 + rnd.nextInt(19000);
            Cloudlet c = new Cloudlet(i, length, 1, 300, 300, full, full, full);
            c.setUserId(userId);
            cloudlets.add(c);
        }
        broker.submitCloudletList(cloudlets);
        double[][] etc = new double[tasks][vms];
        for (int t = 0; t < tasks; t++)
            for (int v = 0; v < vms; v++)
                etc[t][v] = cloudlets.get(t).getCloudletLength()
                          / (vmList.get(v).getMips() * vmList.get(v).getNumberOfPes());
        long t0 = System.nanoTime();
        int[] map = scheduler.schedule(etc);
        Result res = new Result();
        res.schedMs = (System.nanoTime() - t0) / 1e6;
        for (int t = 0; t < tasks; t++)
            broker.bindCloudletToVm(cloudlets.get(t).getCloudletId(), vmList.get(map[t]).getId());
        CloudSim.startSimulation();
        List<Cloudlet> done = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        double sum = 0;
        for (Cloudlet c : done) {
            res.simulated = Math.max(res.simulated, c.getFinishTime());
            sum += c.getFinishTime();
        }
        res.avgCompletion = sum / done.size();
        double[] load = new double[vms];
        for (int t = 0; t < tasks; t++) load[map[t]] += etc[t][map[t]];
        double mx = 0, mn = Double.MAX_VALUE, avg = 0;
        for (double l : load) { mx = Math.max(mx, l); mn = Math.min(mn, l); avg += l / vms; }
        res.imbalance = (mx - mn) / avg;
        return res;
    }

    static void createDatacenter(String name) throws Exception {
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Pe> pes = new ArrayList<>();
            for (int p = 0; p < 8; p++) pes.add(new Pe(p, new PeProvisionerSimple(5000)));
            hosts.add(new Host(i, new RamProvisionerSimple(65536), new BwProvisionerSimple(100000),
                               1000000, pes, new VmSchedulerTimeShared(pes)));
        }
        DatacenterCharacteristics ch = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hosts, 10.0, 3.0, 0.05, 0.001, 0.0);
        new Datacenter(name, ch, new VmAllocationPolicySimple(hosts), new LinkedList<Storage>(), 0);
    }
}