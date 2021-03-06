package com.iota.iri.service.tipselection.impl;

import com.iota.iri.MilestoneTracker;
import com.iota.iri.controllers.MilestoneViewModel;
import com.iota.iri.crypto.SpongeFactory;
import com.iota.iri.model.Hash;
import com.iota.iri.model.IntegerIndex;
import com.iota.iri.model.TransactionHash;
import com.iota.iri.service.tipselection.EntryPointSelector;
import com.iota.iri.storage.Tangle;
import com.iota.iri.zmq.MessageQ;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntryPointSelectorImplTest {

    @Mock
    private MilestoneTracker milestoneTracker;
    @Mock
    private Tangle tangle = new Tangle();

    @BeforeClass
    public static void setUp() throws Exception {
        MilestoneViewModel.clear();
    }

    @Test
    public void testEntryPointBWithTangleData() throws Exception {
        Hash milestoneHash = TransactionHash.calculate(SpongeFactory.Mode.CURLP81, new byte[]{1});
        mockTangleBehavior(milestoneHash);
        mockMilestoneTrackerBehavior(0, Hash.NULL_HASH);

        EntryPointSelector entryPointSelector = new EntryPointSelectorImpl(tangle, milestoneTracker);
        Hash entryPoint = entryPointSelector.getEntryPoint(10);

        Assert.assertEquals("The entry point should be the milestone in the Tangle", milestoneTracker.getLatestMilestone(), entryPoint);
    }

    @Test
    public void testEntryPointAWithoutTangleData() throws Exception {
        mockMilestoneTrackerBehavior(0, milestoneTracker.getLatestSolidSbutangleMilestone());
        EntryPointSelector entryPointSelector = new EntryPointSelectorImpl(tangle, milestoneTracker);
        Hash entryPoint = entryPointSelector.getEntryPoint(10);

        Assert.assertEquals("The entry point should be the last tracked solid milestone", milestoneTracker.getLatestMilestone(), entryPoint);
    }


    private void mockMilestoneTrackerBehavior(int latestSolidSubtangleMilestoneIndex, Hash latestSolidSubtangleMilestone) {
        milestoneTracker.setLatestSolidSubtangleMilestoneIndex(latestSolidSubtangleMilestoneIndex);
        milestoneTracker.setLatestSolidSubtangleMilestone(latestSolidSubtangleMilestone);
    }

    private void mockTangleBehavior(Hash milestoneModelHash) throws Exception {
        com.iota.iri.model.persistables.Milestone milestoneModel = new com.iota.iri.model.persistables.Milestone();
        milestoneModel.setIndex(new IntegerIndex(1));
        milestoneModel.setHash(milestoneModelHash);
        Mockito.when(milestoneTracker.getMilestoneStartIndex()).thenReturn(0);
        milestoneTracker.setLatestMilestoneIndex(1);
        Mockito.when(tangle.load(com.iota.iri.model.persistables.Milestone.class, milestoneModel.getIndex()))
                .thenReturn(milestoneModel);
    }
}
