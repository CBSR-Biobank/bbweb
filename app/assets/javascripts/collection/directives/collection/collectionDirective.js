/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Description
   *
   * The studyCounts object has the following fields: disabled, enabled, and retired.
   */
  function collectionDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        studyCounts: '=',
        centreCounts: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/collection/collection.html',
      controller: CollectionCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CollectionCtrl.$inject = ['gettextCatalog', 'Study'];

  /**
   *
   */
  function CollectionCtrl(gettextCatalog, Study) {
    var vm = this;

    vm.haveEnabledStudies           = (vm.studyCounts.enabled > 0);
    vm.haveEnabledCentres           = (vm.centreCounts.enabled > 0);
    vm.isCollectionAllowed          = vm.haveEnabledStudies && vm.haveEnabledCentres;
    vm.updateEnabledStudies         = updateEnabledStudies;
    vm.getEnabledStudiesPanelHeader = getEnabledStudiesPanelHeader;

    //---

    function updateEnabledStudies(options) {
      return Study.list(options);
    }

    function getEnabledStudiesPanelHeader() {
      return gettextCatalog.getString('Studies you participate in');
    }

  }

  return collectionDirective;
});
