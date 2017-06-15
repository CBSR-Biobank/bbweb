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

  CollectionController.$inject = ['$state', 'gettextCatalog', 'Study', 'breadcrumbService'];

  /*
   * Controller for this component.
   *
   * The studyCounts object has the following fields: disabled, enabled, and retired.
   */
  function CollectionController($state, gettextCatalog, Study, breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
    ];

    vm.$onInit              = onInit;
    vm.isCollectionAllowed  = false;
    vm.updateEnabledStudies  = updateEnabledStudies;
    vm.getStudiesPanelHeader = getStudiesPanelHeader;

    //---

    function onInit() {
      Study.collectionStudies()
        .then(function (reply) {
          vm.isCollectionAllowed = (reply.items.length > 0);
        })
        .catch(function (error) {
          if (error.status && (error.status === 401)) {
            $state.go('home.users.login', {}, { reload: true });
          }
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
