/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/collection/components/collection/collection.html',
    controller: CollectionController,
    controllerAs: 'vm'
  };

  CollectionController.$inject = ['gettextCatalog', 'Study'];

  /*
   * Controller for this component.
   *
   * The studyCounts object has the following fields: disabled, enabled, and retired.
   */
  function CollectionController(gettextCatalog, Study) {
    var vm = this;

    vm.$onInit              = onInit;
    vm.isCollectionAllowed  = false;
    vm.updateEnabledStudies  = updateEnabledStudies;
    vm.getStudiesPanelHeader = getStudiesPanelHeader;

    //---

    function onInit() {
      Study.collectionStudies().then(function (reply) {
        vm.isCollectionAllowed = (reply.items.length > 0);
      });
    }

    // invoked by the SelectStudy directive
    function updateEnabledStudies(options) {
      return Study.collectionStudies(options);
    }

    function getStudiesPanelHeader() {
      return gettextCatalog.getString('Studies you participate in');
    }


  }

  return component;
});
