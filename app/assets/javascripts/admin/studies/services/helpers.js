/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('studies.helpers', []);

  /**
   * Used to sort a list of studies. Returns the comparison of names of the two studies.
   */
  mod.service('studyCompareService', function() {
    this.compare = function (a, b) {
      if (a.name < b.name) {
        return -1;
      } else if (a.name > b.name) {
        return 1;
      }
      return 0;
    };
  });

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('addTimeStampsService', function() {
    return {
      get: function(modelObj) {
        var data = [];
        data.push({name: 'Date added:', value: modelObj.addedDate});
        if (modelObj.lastUpdateDate !== null) {
          data.push({name: 'Last updated:', value: modelObj.lastUpdateDate});
        }
        return data;
      }
    };
  });

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('annotTypeModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (annotType) {
        var title = 'Participant Annotation Type';
        var data = [];
        data.push({name: 'Name:', value: annotType.name});
        data.push({name: 'Type:', value: annotType.valueType});

        if (!annotType.required) {
          data.push({name: 'Required:', value: annotType.required ? "Yes" : "No"});
        }

        if (annotType.valueType === 'Select') {
          var optionValues = [];
          for (var name in annotType.options) {
            optionValues.push(annotType.options[name]);
          }

          data.push({
            name: '# Selections Allowed:',
            value: annotType.maxValueCount === 1 ? "Single" : "Multiple"});
          data.push({
            name: 'Selections:',
            value: optionValues.join(", ")});
        }

        data.push({name: 'Description:', value: annotType.description});
        data = data.concat(addTimeStampsService.get(annotType));

        modelObjModalService.show(title, data);
      };
    }]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('specimenGroupModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (specimenGroup) {
        var title = 'Specimen Group';
        var data = [];
        data.push({name: 'Name:', value: specimenGroup.name});
        data.push({name: 'Units:', value: specimenGroup.units});
        data.push({name: 'Anatomical Source:', value: specimenGroup.anatomicalSourceType});
        data.push({name: 'Preservation Type:', value: specimenGroup.preservationType});
        data.push({name: 'Preservation Temperature:', value: specimenGroup.preservationTemperatureType});
        data.push({name: 'Specimen Type:', value: specimenGroup.specimenType});
        data.push({name: 'Description:', value: specimenGroup.description});
        data = data.concat(addTimeStampsService.get(specimenGroup));
        modelObjModalService.show(title, data);
      };
    }]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('ceventTypeModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (ceventType) {
        var title = 'Collection Event Type';
        var data = [];
        data.push({name: 'Name:', value: ceventType.name});
        data.push({name: 'Recurring:', value: ceventType.recurring});
        data.push({name: 'Specimen Groups:', value: ceventType.specimenGroupData});
        data.push({name: 'Annotation Types:', value: ceventType.annotationTypeData});
        data.push({name: 'Description:', value: ceventType.description});
        data = data.concat(addTimeStampsService.get(ceventType));
        modelObjModalService.show(title, data);
      };
    }]);

  mod.service('panelTableService', ['ngTableParams', '$filter', function (ngTableParams, $filter) {
    this.getTableParams = function(data) {
      /* jshint ignore:start */
      return new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      },{
        counts: [], // hide page counts control
        total: function() { return data.length; },
        getData: function($defer, params) {
          params.total(data.length);
          var orderedData = params.sorting()
            ? $filter('orderBy')(data, params.orderBy())
            : data;
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        }
      });
      /* jshint ignore:end */
    };
  }]);

  mod.service('studyAnnotTypeRemoveService', ['modalService', function (modalService) {
    this.remove = function (title, message, onConfirm, onCancel) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerText: title,
        bodyText: message
      };

      modalService.showModal({}, modalOptions).then(function (result) {
        onConfirm();
      }, function() {
        onCancel();
      });
    };
  }]);

  /**
   * Removes a participant annotation type.
   */
  mod.service('participantAnnotTypeRemoveService', [
    'studyAnnotTypeRemoveService', 'ParticipantAnnotTypeService', 'modalService',
    function (studyAnnotTypeRemoveService, ParticipantAnnotTypeService, modalService) {
      return {
        remove: function($state, $stateParams, annotType) {
          studyAnnotTypeRemoveService.remove(
            'Remove Participant Annotation Type',
            'Are you sure you want to remove annotation type ' + annotType.name + '?',
            function (result) {
              ParticipantAnnotTypeService.remove(annotType)

                .success(function() {
                  // could use $state.reload() here but it does not re-initialize the
                  // controller
                  $state.transitionTo($state.current, $stateParams, {
                    reload: true,
                    inherit: false,
                    notify: true
                  });
                })

                .error(function(error) {
                  var modalOptions = {
                    closeButtonText: 'Cancel',
                    headerText: 'Remove failed',
                    bodyText: 'Annotation type ' + annotType.name + ' cannot be removed: ' + error.message
                  };

                  modalService.showModal({}, modalOptions).then(function (result) {
                    $state.go('admin.studies.study.participants');
                  }, function () {
                    $state.go('admin.studies.study.participants');
                  });
                });
            },
            function() {
              $state.go('admin.studies.study.participants');
            });
        }
      };
    }]);

  /**
   * Removes a specimen group.
   */
  mod.service('specimenGroupRemoveService', [
    'studyAnnotTypeRemoveService', 'SpecimenGroupService', 'modalService',
    function (studyAnnotTypeRemoveService, SpecimenGroupService, modalService) {
      return {
        remove: function($state, $stateParams, specimenGroup) {
          studyAnnotTypeRemoveService.remove(
            'Remove Specimen Group',
            'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
            function (result) {
              SpecimenGroupService.remove(specimenGroup)

                .success(function() {
                  // could use $state.reload() here but it does not re-initialize the
                  // controller
                  $state.transitionTo($state.current, $stateParams, {
                    reload: true,
                    inherit: false,
                    notify: true
                  });
                })

                .error(function(error) {
                  var modalOptions = {
                    closeButtonText: 'Cancel',
                    headerText: 'Remove failed',
                    bodyText: 'Specimen group ' + specimenGroup.name + ' cannot be removed: ' + error.message
                  };

                  modalService.showModal({}, modalOptions).then(function (result) {
                    $state.go('admin.studies.study.specimens');
                  }, function () {
                    $state.go('admin.studies.study.specimens');
                  });
                });
            },
            function() {
              $state.go('admin.studies.study.specimens');
            });
        }
      };
    }]);

  /**
   * Removes a specimen group.
   */
  mod.service('ceventTypeRemoveService', [
    'studyAnnotTypeRemoveService', 'CollectionEventTypeService', 'modalService',
    function (studyAnnotTypeRemoveService, CollectionEventTypeService, modalService) {
      return {
        remove: function($state, $stateParams, ceventType) {
          studyAnnotTypeRemoveService.remove(
            'Remove Collection Event Type',
            'Are you sure you want to remove collection event type ' + ceventType.name + '?',
            function (result) {
              CollectionEventTypeService.remove(ceventType)

                .success(function() {
                  // could use $state.reload() here but it does not re-initialize the
                  // controller
                  $state.transitionTo($state.current, $stateParams, {
                    reload: true,
                    inherit: false,
                    notify: true
                  });
                })

                .error(function(error) {
                  var modalOptions = {
                    closeButtonText: 'Cancel',
                    headerText: 'Remove failed',
                    bodyText: 'Collection event type ' + ceventType.name + ' cannot be removed: ' + error.message
                  };

                  modalService.showModal({}, modalOptions).then(function (result) {
                    $state.go('admin.studies.study.collection');
                  }, function () {
                    $state.go('admin.studies.study.collection');
                  });
                });
            },
            function() {
              $state.go('admin.studies.study.collection');
            });
        }
      };
    }]);

  /**
   * Removes a specimen group.
   */
  mod.service('ceventAnnotTypeRemoveService', [
    'studyAnnotTypeRemoveService', 'CollectionEventTypeService', 'modalService',
    function (studyAnnotTypeRemoveService, CollectionEventTypeService, modalService) {
      return {
        remove: function($state, $stateParams, ceventAnnotType) {
          studyAnnotTypeRemoveService.remove(
            'Remove Collection Event Annotation Type',
            'Are you sure you want to remove collection event annotation type ' + ceventAnnotType.name + '?',
            function (result) {
              CollectionEventAnnotationTypeService.remove(ceventAnnotType)

                .success(function() {
                  // could use $state.reload() here but it does not re-initialize the
                  // controller
                  $state.transitionTo($state.current, $stateParams, {
                    reload: true,
                    inherit: false,
                    notify: true
                  });
                })

                .error(function(error) {
                  var modalOptions = {
                    closeButtonText: 'Cancel',
                    headerText: 'Remove failed',
                    bodyText: 'Collection event annotation type ' + ceventAnnotType.name + ' cannot be removed: ' + error.message
                  };

                  modalService.showModal({}, modalOptions).then(function (result) {
                    $state.go('admin.studies.study.collection');
                  }, function () {
                    $state.go('admin.studies.study.collection');
                  });
                });
            },
            function() {
              $state.go('admin.studies.study.collection');
            });
        }
      };
    }]);

  return mod;
});
