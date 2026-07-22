package com.huadong.pipeline.service;

import com.huadong.pipeline.api.MilestoneApi;
import com.huadong.pipeline.api.MilestoneApi.MilestoneUpdateRequest;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneUpdateCommand;
import com.huadong.pipeline.manager.MilestoneManager;
import com.huadong.pipeline.manager.MilestoneManager.MilestoneNodeState;
import com.huadong.pipeline.manager.MilestoneManager.MilestoneResult;
import com.huadong.pipeline.manager.MilestoneManager.StageProjectionResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MilestoneApiService implements MilestoneApi {

  private final MilestoneManager manager;

  public MilestoneApiService(MilestoneManager manager) {
    this.manager = manager;
  }

  @Override
  public MilestonePageResponse getMilestones(long studyId, String username) {
    return page(manager.getMilestones(studyId, username));
  }

  @Override
  public MilestonePageResponse updateMilestone(long studyId, String milestoneCode,
                                               MilestoneUpdateRequest request, String username) {
    var cmd = new MilestoneUpdateCommand(
        request.planV1Date(), request.planV2Date(),
        request.actualStartDate(), request.actualEndDate(),
        request.deviationNote());
    return page(manager.updateMilestone(studyId, milestoneCode, cmd, username));
  }

  @Override
  public StageProjectionResponse getStageProjection(long studyId, String username) {
    StageProjectionResult result = manager.getStageProjection(studyId, username);
    return new StageProjectionResponse(
        result.currentStageCode(), result.currentStageName(),
        result.currentMilestoneCode(), result.currentMilestoneName(),
        result.statusText());
  }

  // ──────────── mapping helpers ────────────

  private static MilestonePageResponse page(MilestoneResult result) {
    // Group nodes by stage code, preserving the definition order
    Map<String, List<MilestoneNodeResponse>> grouped = new LinkedHashMap<>();
    Map<String, String> stageNames = new LinkedHashMap<>();
    for (MilestoneNodeState node : result.nodes()) {
      stageNames.putIfAbsent(node.stageCode(), node.stageLabel());
      grouped.computeIfAbsent(node.stageCode(), k -> new ArrayList<>())
          .add(new MilestoneNodeResponse(
              node.milestoneCode(), node.milestoneLabel(),
              node.planV1Date(), node.planV2Date(),
              node.actualStartDate(), node.actualEndDate(),
              node.status(), node.deviationNote()));
    }
    List<StageGroupResponse> groups = new ArrayList<>();
    for (var entry : grouped.entrySet()) {
      groups.add(new StageGroupResponse(
          entry.getKey(), stageNames.get(entry.getKey()), entry.getValue()));
    }
    return new MilestonePageResponse(result.studyCode(), groups);
  }
}
