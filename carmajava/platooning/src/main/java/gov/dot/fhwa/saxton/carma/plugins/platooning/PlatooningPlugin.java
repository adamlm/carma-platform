/*
 * Copyright (C) 2017 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gov.dot.fhwa.saxton.carma.plugins.platooning;

import java.util.LinkedList;
import java.util.List;

import gov.dot.fhwa.saxton.carma.guidance.arbitrator.TrajectoryPlanningResponse;
import gov.dot.fhwa.saxton.carma.guidance.plugins.AbstractPlugin;
import gov.dot.fhwa.saxton.carma.guidance.plugins.PluginServiceLocator;
import gov.dot.fhwa.saxton.carma.guidance.trajectory.Trajectory;

public class PlatooningPlugin extends AbstractPlugin {

    protected final String PLATOONING_FLAG = "PLATOONING";
    
    protected IPlatooningState state = new StandbyState();
    protected PlatoonManager manager = new PlatoonManager(this);
    protected List<PlatoonMember> platoon = new LinkedList<>();
    
    protected CommandGenerator commandGenerator = null;
    protected Thread commandGeneratorThread = null;
    
    public PlatooningPlugin(PluginServiceLocator pluginServiceLocator) {
        super(pluginServiceLocator);
        version.setName("CACC Platooning Plugin");
        version.setMajorRevision(0);
        version.setIntermediateRevision(0);
        version.setMinorRevision(1);
    }

    @Override
    public void onInitialize() {
        log.info("CACC platooning pulgin is initializing...");
    }

    @Override
    public void onResume() {
        if(commandGenerator == null && commandGeneratorThread == null) {
            commandGenerator = new CommandGenerator(this);
            commandGeneratorThread = new Thread(commandGenerator);
            commandGeneratorThread.setName("Platooning Command Generator");
            commandGeneratorThread.start();
        }
        this.setAvailability(true);
    }

    @Override
    public void loop() throws InterruptedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSuspend() {
        this.setAvailability(false);
        if(commandGenerator != null && commandGeneratorThread != null) {
            commandGeneratorThread.interrupt();
            commandGenerator = null;
            commandGeneratorThread = null;
        }
    }

    @Override
    public void onTerminate() {
        // NO-OP
    }

    protected void setState(IPlatooningState state) {
        log.info(this.getClass().getSimpleName() + "is changing from " + this.state.toString() + " to " + state.toString());
        this.state = state;
    }
    
    @Override
    public TrajectoryPlanningResponse planTrajectory(Trajectory traj, double expectedEntrySpeed) {
        return this.state.planTrajectory(this, log, pluginServiceLocator, traj, expectedEntrySpeed);
    }
    
    @Override
    public void onReceiveNegotiationRequest(String strategy) {
        this.state.onReceiveNegotiationRequest(this, log, pluginServiceLocator, strategy);
    }
}
