/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   *
   */
  function studiesListDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        studyCounts: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studiesList/studiesList.html',
      controller: StudiesListCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudiesListCtrl.$inject = [
    'Study',
    'StudyStatus'
  ];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesListCtrl(Study, StudyStatus) {
    var vm = this;

    vm.pageSize         = 5;
    vm.updateStudies    = Study.list;
    vm.possibleStatuses = [ { id: 'all', label: 'All' } ];

    _.each(StudyStatus.values(), function (status) {
      vm.possibleStatuses.push({id: status, label: StudyStatus.label(status)});
    });
  }

  return studiesListDirective;
});
