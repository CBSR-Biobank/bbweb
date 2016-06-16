/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   * This directive allows the user to link a center to one or more study.
   */
  function centreStudiesPanelDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        centre:     '=',
        studyNames: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/directives/centreStudiesPanel/centreStudiesPanel.html',
      controller: CentreStudiesPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreStudiesPanelCtrl.$inject = [
    '$scope',
    'Panel',
    'Study',
    'StudyViewer',
    'modalService',
    'studyStatusLabel'
  ];

  /**
   *
   */
  function CentreStudiesPanelCtrl($scope,
                                  Panel,
                                  Study,
                                  StudyViewer,
                                  modalService,
                                  studyStatusLabel) {

    var vm = this;

    var panel = new Panel('centre.panel.studies');

    vm.studyNamesById = [];
    vm.tableStudies   = [];
    vm.studyCollection = [];

    vm.remove         = remove;
    vm.information    = information;
    vm.panelOpen      = panel.getPanelOpenState();

    vm.selected = undefined;
    vm.onSelect = onSelect;

    init();

    //--

    function init() {
      // updates the selected tab in 'centreViewDirective' which is the parent directive
      $scope.$emit('centre-view', 'studies-panel-selected');

      $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                    angular.bind(panel, panel.watchPanelOpenChangeFunc));

      vm.studyNamesById = _.indexBy(vm.studyNames, 'id');

      _.each(vm.centre.studyIds, function (studyId) {
        var study = vm.studyNamesById[studyId];
        study.statusLabel = studyStatusLabel.statusToLabel(study.status);
        vm.studyCollection.push(study);
      });
    }

    function onSelect(item) {
      if (!vm.centre.isDisabled()) {
        console.error('Should not be allowed to add studies to centre if centre is not disabled');
        throw new Error('An application error occurred, please contact your administrator.');
      }

      // add the study only if it's not there
      if(_.indexOf(vm.centre.studyIds, item.id) < 0) {
        vm.centre.addStudy(item).then(addSuccessful);
      }
      vm.selected = undefined;

      function addSuccessful(centre) {
        var study = vm.studyNamesById[item.id];
        study.statusLabel = studyStatusLabel.statusToLabel(study.status);
        vm.centre = centre;
        vm.studyCollection.push(study);
      }

    }

    function information(studyId) {
      return Study.get(studyId).then(function (study) {
        vm.studyNamesById[study.id] = study;
        return new StudyViewer(study);
      });
    }

    function remove(studyId) {
      // FIXME should not allow study to be removed if centre holds specimens for study
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Remove study',
        bodyHtml: 'Are you sure you want to remove study ' + vm.studyNamesById[studyId].name + '?'
      };

      modalService.showModal({}, modalOptions).then(function () {
        return vm.centre.removeStudy({id: studyId})
          .then(function () {
            vm.studyCollection = _.without(vm.studyCollection, vm.studyNamesById[studyId]);
          })
          .catch(removeFailed);
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

  return centreStudiesPanelDirective;
});
