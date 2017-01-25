/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * An AngularJS directive that displays a list of studies.
   *
   * @return {object} An AngularJS directive.
   */
  function studiesListDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {},
      templateUrl : '/assets/javascripts/admin/studies/directives/studiesList/studiesList.html',
      controller: StudiesListCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudiesListCtrl.$inject = [
    'gettextCatalog',
    'Study',
    'StudyState',
    'StudyCounts'
  ];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * @param {AngularJs_Service} gettextCatalog - the service that provides string translations functions.
   *
   * @param {domain.studies.Study} Study - The class that can create a study.
   *
   * @param {AngularJs_Constant} StudyState - A constant that enumerates a study's possible states.
   *
   * @param {AngularJs_Factory} StudyCounts - The service that can query the server for the studies in
   * different states.
   *
   * @return {object} An AngularJS controller for this directive.
   */
  function StudiesListCtrl(gettextCatalog,
                           Study,
                           StudyState,
                           StudyCounts) {
    var vm = this;

    vm.studyCounts   = {};
    vm.limit         = 5;
    vm.updateStudies = Study.list;
    vm.getStudyIcon  = getStudyIcon;

    vm.possibleStates = [ { id: 'all', label: gettextCatalog.getString('All') } ].concat(
      _.map(_.values(StudyState), function (state) {
        return { id: state, label: state.toUpperCase() };
      }));

    init();

    //--

    function init() {
      StudyCounts.get().then(function (counts) {
        vm.studyCounts = counts;
      });
    }

    function getStudyIcon(study) {
      if (study.isDisabled()) {
        return 'glyphicon-cog';
      } else if (study.isEnabled()) {
        return 'glyphicon-ok-circle';
      }
      return 'glyphicon-remove-sign';
    }
  }

  return studiesListDirective;
});
