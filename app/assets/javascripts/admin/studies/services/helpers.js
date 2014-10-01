/** Study helpers */
define(['angular', 'underscore', 'common'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.studies.helpers', []);

  mod.service('panelTableService', ['$filter', 'ngTableParams', function ($filter, ngTableParams) {
    this.getTableParams = function(data) {

      /* jshint -W055 */
      return new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      },{
        counts: [], // hide page counts control
        total: data.length,
        getData: function($defer, params) {
          var orderedData = params.sorting() ?
              $filter('orderBy')(data, params.orderBy()) : data;
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        }
      });
      /* jshint +W055 */
    };
  }]);

  /**
   * Called where there was an error on attempting to add or update a study.
   */
  mod.service('studyEditService', [
    '$state', 'modalService', 'stateHelper', 'StudyService',
    function ($state, modalService, stateHelper, StudyService) {

      var onError = function (study, error, onCancel) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this study. Press OK to make ' +
            ' your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText = 'Cannot ' + (study.id ?  'update' : 'add') + ' study';
          modalOptions.bodyText = error.message;
        }

        modalService.showModal({}, modalOptions).then(
          function () {
            stateHelper.reloadAndReinit();
          },
          function () {
            onCancel();
          });
      };

      return {
        edit : function($scope, onSubmitSuccess, onSubmitErrorCancel, onCancel) {
          $scope.submit = function(study) {
            StudyService.addOrUpdate(study).then(
              onSubmitSuccess,
              function(message) {
                onError($scope.study, message, onSubmitErrorCancel);
              });
          };

          $scope.cancel = function() {
            onCancel();
          };
        }
      };
    }
  ]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('addTimeStampsService', ['$filter', function($filter) {
    return {
      get: function(modelObj) {
        var data = [];
        data.push({name: 'Added:', value: $filter('timeago')(modelObj.timeAdded)});
        if (modelObj.timeModified !== null) {
          data.push({name: 'Last updated:', value: $filter('timeago')(modelObj.timeModified)});
        }
        return data;
      }
    };
  }]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('annotTypeModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      return {
        show: function (title, annotType) {
          var data = [];
          data.push({name: 'Name:', value: annotType.name});
          data.push({name: 'Type:', value: annotType.valueType});

          if (typeof annotType.required !== 'undefined') {
            data.push({name: 'Required:', value: annotType.required ? 'Yes' : 'No'});
          }

          if (annotType.valueType === 'Select') {
            data.push({
              name: '# Selections Allowed:',
              value: annotType.maxValueCount === 1 ? 'Single' : 'Multiple'
            });
            data.push({
              name: 'Selections:',
              value: annotType.options.join(', ')
            });
          }

          data.push({name: 'Description:', value: annotType.description});
          data = data.concat(addTimeStampsService.get(annotType));

          modelObjModalService.show(title, data);
        }
      };
    }
  ]);

  mod.service('studyRemoveModalService', [
    '$state', 'modalService', function ($state, modalService) {
      return {
        remove: function (title, message) {
          var modalOptions = {
            closeButtonText: 'Cancel',
            headerText: title,
            bodyText: message
          };

          return modalService.showModal({}, modalOptions);
        },
        onError: function(bodyText, onModalOkState, onModalCancelState) {
          var modalOptions = {
            closeButtonText: 'Cancel',
            headerText: 'Remove failed',
            bodyText: bodyText
          };

          modalService.showModal({}, modalOptions).then(
            function () {
            $state.go(onModalOkState);
          }, function () {
            $state.go(onModalCancelState);
          });
        }
      };
    }
  ]);

  mod.factory('PanelSettings', [
    'studyViewSettings', 'panelTableService',
    function(studyViewSettings, panelTableService) {
      var PanelSettings = function(panelStateName, data) {
        if (arguments.length > 0) {
          this.init(panelStateName, data);
        }
      };

      PanelSettings.prototype.init = function(panelStateName, data) {
        this.panelStateName = panelStateName;
        this.data = data;
        this.tableParams = panelTableService.getTableParams(this.data);
        this.panelOpen = studyViewSettings.panelState(panelStateName);
      };

      PanelSettings.prototype.panelToggle = function() {
        return studyViewSettings.panelStateToggle(this.panelStateName);
      };

      return PanelSettings;
    }
  ]);

  mod.factory('AnnotTypesPanelSettings', [
    'PanelSettings', 'annotTypeModalService',
    function(PanelSettings, annotTypeModalService) {

      var AnnotTypesPanelSettings = function (panelStateName,
                                              annotTypes,
                                              title,
                                              header,
                                              hasRequiredField,
                                              onAdd,
                                              onUpdate,
                                              onRemove) {
        if (arguments.length > 0) {
          this.init(panelStateName, annotTypes);

          this.title       = title;
          this.header      = header;
          this.hasRequired = hasRequiredField;
          this.add         = onAdd;
          this.update      = onUpdate;
          this.remove      = onRemove;

          this.columns = [
            { title: 'Name', field: 'name', filter: { 'name': 'text' } },
            { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
          ];
          if (hasRequiredField) {
            this.columns.push(
              { title: 'Required', field: 'required', filter: { 'required': 'text' } });
          }
          this.columns.push(
            { title: 'Description', field: 'description', filter: { 'description': 'text' } });
        }
      };

      AnnotTypesPanelSettings.prototype = new PanelSettings();
      AnnotTypesPanelSettings.prototype.constructor = AnnotTypesPanelSettings;
      AnnotTypesPanelSettings.constructor = PanelSettings.prototype.constructor;

      AnnotTypesPanelSettings.prototype.information = function(annotType) {
        annotTypeModalService.show(this.title, annotType);
      };

      AnnotTypesPanelSettings.prototype.remove = function(annotType) {
        this.onRemove(annotType);
      };

      return AnnotTypesPanelSettings;
    }
  ]);

  mod.factory('SpecimenGroupsPanelSettings',  [
    '$state',
    'PanelSettings',
    'specimenGroupModalService',
    'specimenGroupRemoveService',
    function($state,
             PanelSettings,
             specimenGroupModalService,
             specimenGroupRemoveService) {

      var SpecimenGroupsPanelSettings = function (panelStateName, specimenGroups) {
        this.init(panelStateName, specimenGroups);
        this.title = 'Specimen Groups';
        this.header = ' A Specimen Group is used to configure a specimen type to be used by the study. ' +
          'It records ownership, summary, storage, and classification information that applies ' +
          'to an entire group or collection of Specimens.';
      };

      SpecimenGroupsPanelSettings.prototype = new PanelSettings();
      SpecimenGroupsPanelSettings.prototype.constructor = SpecimenGroupsPanelSettings;
      SpecimenGroupsPanelSettings.constructor = PanelSettings.prototype.constructor;

      SpecimenGroupsPanelSettings.prototype.information = function(specimenGroup) {
        specimenGroupModalService.show(specimenGroup);
      };

      SpecimenGroupsPanelSettings.prototype.add = function() {
        $state.go('admin.studies.study.specimens.groupAdd');
      };

      SpecimenGroupsPanelSettings.prototype.update = function(specimenGroup) {
        $state.go('admin.studies.study.specimens.groupUpdate', { specimenGroupId: specimenGroup.id });
      };

      SpecimenGroupsPanelSettings.prototype.remove = function(specimenGroup) {
        specimenGroupRemoveService.remove(specimenGroup);
      };

      return SpecimenGroupsPanelSettings;
    }
  ]);

  mod.factory('CeventTypesPanelSettings',  [
    '$state',
    'PanelSettings',
    'ceventTypeModalService',
    'ceventTypeRemoveService',
    'specimenGroupModalService',
    'studyViewSettings',
    function($state,
             PanelSettings,
             ceventTypeModalService,
             ceventTypeRemoveService,
             specimenGroupModalService,
             studyViewSettings) {

      var CeventTypesPanelSettings = function(panelStateName, ceventTypes, specimenGroups, annotTypes) {
        // push all specimen groups names into an array for easy display
        var self = this;
        this.specimenGroupsById = _.indexBy(specimenGroups, 'id');
        this.init('ceventTypes', ceventTypes);
        this.specimenGroups = specimenGroups;
        this.annotTypes = annotTypes;
        this.panelOpen = studyViewSettings.panelState('ceventTypes');

        ceventTypes.forEach(function (cet) {
          cet.specimenGroups = [];
          cet.specimenGroupData.forEach(function (sgItem) {
            var sg = self.specimenGroupsById[sgItem.specimenGroupId];
            cet.specimenGroups.push({ id: sgItem.specimenGroupId, name: sg.name });
          });
        });
      };

      CeventTypesPanelSettings.prototype = new PanelSettings();
      CeventTypesPanelSettings.prototype.constructor = CeventTypesPanelSettings;
      CeventTypesPanelSettings.constructor = PanelSettings.prototype.constructor;

      CeventTypesPanelSettings.prototype.information = function(ceventType) {
        ceventTypeModalService.show(ceventType, this.specimenGroups, this.annotTypes);
      };

      CeventTypesPanelSettings.prototype.add = function() {
        $state.go('admin.studies.study.collection.ceventTypeAdd');
      };

      CeventTypesPanelSettings.prototype.update = function(ceventType) {
        $state.go('admin.studies.study.collection.ceventTypeUpdate',
                  { studyId: ceventType.studyId, ceventTypeId: ceventType.id });
      };

      CeventTypesPanelSettings.prototype.remove = function(ceventType) {
        ceventTypeRemoveService.remove(ceventType);
      };

      CeventTypesPanelSettings.prototype.showSpecimenGroup = function (specimenGroupId) {
        specimenGroupModalService.show(this.specimenGroupsById[specimenGroupId]);
      };

      return CeventTypesPanelSettings;
    }
  ]);

  mod.factory('ProcessingTypesPanelSettings',  [
    '$state', 'PanelSettings', 'processingTypeModalService', 'processingTypeRemoveService', 'studyViewSettings',
    function($state, PanelSettings, processingTypeModalService, processingTypeRemoveService, studyViewSettings) {

      var ProcessingTypesPanelSettings = function (panelStateName, processingTypes) {
        this.init(panelStateName, processingTypes);
        this.panelOpen = studyViewSettings.panelState('processingTypes');
      };

      ProcessingTypesPanelSettings.prototype = new PanelSettings();
      ProcessingTypesPanelSettings.prototype.constructor = ProcessingTypesPanelSettings;
      ProcessingTypesPanelSettings.constructor = PanelSettings.prototype.constructor;

      ProcessingTypesPanelSettings.prototype.information = function(processingType) {
        processingTypeModalService.show(processingType);
      };

      ProcessingTypesPanelSettings.prototype.add = function() {
        $state.go('admin.studies.study.processing.processingTypeAdd');
      };

      ProcessingTypesPanelSettings.prototype.update = function(processingType) {
        $state.go('admin.studies.study.processing.processingTypeUpdate',
                  { studyId: processingType.studyId, processingTypeId: processingType.id });
      };

      ProcessingTypesPanelSettings.prototype.remove = function(processingType) {
        processingTypeRemoveService.remove(processingType);
      };

      return ProcessingTypesPanelSettings;
    }
  ]);

  mod.factory('SpcLinkTypesPanelSettings',  [
    '$state',
    'PanelSettings',
    'spcLinkTypeModalService',
    'spcLinkTypeRemoveService',
    'processingTypeModalService',
    'specimenGroupModalService',
    'annotTypeModalService',
    'studyViewSettings',
    function($state,
             PanelSettings,
             spcLinkTypeModalService,
             spcLinkTypeRemoveService,
             processingTypeModalService,
             specimenGroupModalService,
             annotTypeModalService,
             studyViewSettings) {

      var SpcLinkTypesPanelSettings = function (
        panelStateName, processingTypesById, specimenGroupsById, annotTypesById, data) {
        this.init(panelStateName, data);
        this.panelOpen = studyViewSettings.panelState('spcLinkTypes');
        this.processingTypesById = processingTypesById;
        this.specimenGroupsById  = specimenGroupsById;
        this.annotTypesById      = annotTypesById;
      };

      SpcLinkTypesPanelSettings.prototype = new PanelSettings();
      SpcLinkTypesPanelSettings.prototype.constructor = SpcLinkTypesPanelSettings;
      SpcLinkTypesPanelSettings.constructor = PanelSettings.prototype.constructor;

      SpcLinkTypesPanelSettings.prototype.information = function(spcLinkType) {
        spcLinkTypeModalService.show(spcLinkType, this.processingTypesById, this.specimenGroupsById);
      };

      SpcLinkTypesPanelSettings.prototype.add = function() {
        $state.go('admin.studies.study.processing.spcLinkTypeAdd');
      };

      SpcLinkTypesPanelSettings.prototype.update = function(spcLinkType) {
        $state.go('admin.studies.study.processing.spcLinkTypeUpdate',
                  { procTypeId: spcLinkType.processingTypeId, spcLinkTypeId: spcLinkType.id });
      };

      SpcLinkTypesPanelSettings.prototype.remove = function(spcLinkType) {
        spcLinkTypeRemoveService.remove(spcLinkType);
      };

      SpcLinkTypesPanelSettings.prototype.showProcessingType = function (processingTypeId) {
        processingTypeModalService.show(this.processingTypesById[processingTypeId]);
      };

      SpcLinkTypesPanelSettings.prototype.showSpecimenGroup = function (specimenGroupId) {
        specimenGroupModalService.show(this.specimenGroupsById[specimenGroupId]);
      };

      SpcLinkTypesPanelSettings.prototype.showAnnotationType = function (annotTypeId) {
        annotTypeModalService.show('Specimen Link Annotation Type', this.annotTypesById[annotTypeId]);
      };

      return SpcLinkTypesPanelSettings;
    }
  ]);

  return mod;
});
