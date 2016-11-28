/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
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
   */
  function StudiesListCtrl(gettextCatalog,
                           Study,
                           StudyState,
                           StudyCounts) {
    var vm = this;

    vm.studyCounts      = {};
    vm.limit         = 5;
    vm.updateStudies    = Study.list;
    vm.getStudyIcon     = getStudyIcon;

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
