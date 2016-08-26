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
    'gettext',
    'Study',
    'StudyStatus',
    'studyStatusLabel',
    'StudyCounts'
  ];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesListCtrl(gettext,
                           Study,
                           StudyStatus,
                           studyStatusLabel,
                           StudyCounts) {
    var vm = this;

    vm.studyCounts      = {};
    vm.pageSize         = 5;
    vm.updateStudies    = Study.list;
    vm.getStudyIcon     = getStudyIcon;

    vm.possibleStatuses = [ { id: 'all', label: gettext('All') } ];

    _.each(_.values(StudyStatus), function (status) {
      vm.possibleStatuses.push({id: status, label: studyStatusLabel.statusToLabel(status)});
    });

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
