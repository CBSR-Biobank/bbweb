/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash'], function(angular, _) {
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
    '$log',
    'gettextCatalog',
    'Panel',
    'Study',
    'StudyViewer',
    'modalService'
  ];

  /**
   *
   */
  function CentreStudiesPanelCtrl($scope,
                                  $log,
                                  gettextCatalog,
                                  Panel,
                                  Study,
                                  StudyViewer,
                                  modalService) {

    var vm = this;

    vm.studyNamesById = [];
    vm.tableStudies   = [];
    vm.studyCollection = [];

    vm.remove         = remove;
    vm.information    = information;

    vm.selected = undefined;
    vm.onSelect = onSelect;

    init();

    //--

    function init() {
      // updates the selected tab in 'centreViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'studies-panel-selected');

      vm.studyNamesById = _.keyBy(vm.studyNames, 'id');

      _.each(vm.centre.studyIds, function (studyId) {
        var study = vm.studyNamesById[studyId];
        vm.studyCollection.push(study);
      });
    }

    function onSelect(item) {
      if (!vm.centre.isDisabled()) {
        $log.error('Should not be allowed to add studies to centre if centre is not disabled');
        throw new Error('An application error occurred, please contact your administrator.');
      }

      // add the study only if it's not there
      if(_.indexOf(vm.centre.studyIds, item.id) < 0) {
        vm.centre.addStudy(item).then(addSuccessful);
      }
      vm.selected = undefined;

      function addSuccessful(centre) {
        var study = vm.studyNamesById[item.id];
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
      modalService.modalOkCancel(
        gettextCatalog.getString('Remove study'),
        gettextCatalog.getString(
          'Are you sure you want to remove study {{name}}?',
          { name: vm.studyNamesById[studyId].name }))
        .then(function () {
          return vm.centre.removeStudy({id: studyId})
            .then(function () {
              vm.studyCollection = _.without(vm.studyCollection, vm.studyNamesById[studyId]);
            })
            .catch(function (error) {
              modalService.modalOkCancel(gettextCatalog.getString('Remove failed'),
                                         gettextCatalog.getString('Could not remove study: ') + error);
            });
        });
    }

  }

  return centreStudiesPanelDirective;
});
