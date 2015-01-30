define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('centreStudiesPanel', centreStudiesPanel);

  /**
   *
   */
  function centreStudiesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'EA',
      scope: {
        centre: '=',
        centreStudies: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/studiesPanel.html',
      controller: 'CentreStudiesPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('CentreStudiesPanelCtrl', CentreStudiesPanelCtrl);

  CentreStudiesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$filter',
    'panelService',
    'panelTableService',
    'centresService',
    'studyModalService',
    'modalService'
  ];

  /**
   *
   */
  function CentreStudiesPanelCtrl($scope,
                                  $state,
                                  $filter,
                                  panelService,
                                  panelTableService,
                                  centresService,
                                  studyModalService,
                                  modalService) {

    var vm = this;

    var helper = panelService.panel('centre.panel.studies');

    vm.centre         = $scope.centre;
    vm.allStudies     = [];
    vm.studiesById    = _.indexBy($scope.allStudies, 'id');
    vm.centreStudies  = undefined;

    vm.remove         = remove;
    vm.information    = information;
    vm.panelOpen      = helper.panelOpen;
    vm.panelToggle    = helper.panelToggle;

    vm.tableParams = panelTableService.getTableParamsWithCallback(getTableData, {}, {counts: []});
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

    vm.selected = undefined;
    vm.onSelect = onSelect;

    init();

    //--

    function init() {
      vm.centreStudies  = [];
      _.each($scope.centreStudies, function (studyId) {
        vm.centreStudies.push(vm.studiesById[studyId]);
      });
    }

    function getTableData() {
      return vm.centreStudies;
    }

    function onSelect(item) {
      // add the study only if it's not there
      if(_.findWhere(vm.centreStudies, {name: item.name}) === undefined) {
        centresService.addStudy(vm.centre.id, item.id).then(function () {
          vm.centreStudies.push(item);
          vm.tableParams.reload();
        });
      }
      vm.selected = undefined;
    }

    function information(study) {
      studyModalService.show(study);
    }

    function remove(study) {
      // FIXME should not allow study to be removed if centre holds specimens for study
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Remove study',
        bodyHtml: 'Are you sure you want to remove study ' + study.name + '?'
      };

      modalService.showModal({}, modalOptions).then(function () {
        return centresService.removeStudy(vm.centre.id, study.id)
          .then(function () {
            vm.centreStudies = _.without(vm.centreStudies, study);
            vm.tableParams.reload();
          }).
          catch(removeFailed);
      });

      function removeFailed(error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          headerHtml: 'Remove failed',
          bodyHtml: 'Could not remove study: ' + error
        };

        modalService.showModal({}, modalOptions);
      }
    }

  }

});
