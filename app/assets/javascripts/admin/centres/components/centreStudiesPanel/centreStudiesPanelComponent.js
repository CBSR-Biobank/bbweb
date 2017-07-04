/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Allows the user to link a center to one or more study.
   */
  var component = {
    templateUrl: '/assets/javascripts/admin/centres/components/centreStudiesPanel/centreStudiesPanel.html',
    controller: CentreStudiesPanelController,
    controllerAs: 'vm',
    bindings: {
      centre:     '=',
      studyNames: '='
    }
  };

  CentreStudiesPanelController.$inject = [
    '$scope',
    '$log',
    'gettextCatalog',
    'Panel',
    'Study',
    'StudyViewer',
    'modalService'
  ];

  /*
   * Controller for this component.
   */
  function CentreStudiesPanelController($scope,
                                        $log,
                                        gettextCatalog,
                                        Panel,
                                        Study,
                                        StudyViewer,
                                        modalService) {

    var vm = this;

    vm.$onInit        = onInit;
    vm.studyNamesById = [];
    vm.tableStudies   = [];
    vm.studyCollection = [];

    vm.remove         = remove;
    vm.information    = information;

    vm.selected = undefined;
    vm.onSelect = onSelect;

    //--

    function onInit() {
      // updates the selected tab in 'centreViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');

      vm.studyNamesById = _.keyBy(vm.studyNames, 'id');

      _.each(vm.centre.studyIds, function (studyId) {
        var study = vm.studyNamesById[studyId];
        vm.studyCollection.push(study);
      });
    }

    function onSelect(study) {
      if (!vm.centre.isDisabled()) {
        $log.error('Should not be allowed to add studies to centre if centre is not disabled');
        throw new Error('An application error occurred, please contact your administrator.');
      }

      // add the study only if it's not there
      if(_.indexOf(vm.centre.studyIds, study.id) < 0) {
        vm.centre.addStudy(study).then(addSuccessful);
      }
      vm.selected = undefined;

      function addSuccessful(centre) {
        vm.centre = centre;
        vm.studyCollection.push(vm.studyNamesById[study.id]);
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

  return component;
});
