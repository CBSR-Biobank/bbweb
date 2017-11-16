/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./collection.html'),
  controller: CollectionController,
  controllerAs: 'vm'
};

/*
 * Controller for this component.
 *
 * The studyCounts object has the following fields: disabled, enabled, and retired.
 */
/* @ngInject */
function CollectionController($state,
                              gettextCatalog,
                              Study,
                              breadcrumbService,
                              resourceErrorService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
    ];

    vm.isCollectionAllowed  = false;
    vm.updateEnabledStudies  = updateEnabledStudies;
    vm.getStudiesPanelHeader = getStudiesPanelHeader;

    Study.collectionStudies()
      .then(function (reply) {
        vm.isCollectionAllowed = (reply.items.length > 0);
      })
      .catch(resourceErrorService.checkUnauthorized());
  }

  // invoked by the SelectStudy directive
  function updateEnabledStudies(options) {
    return Study.collectionStudies(options);
  }

  function getStudiesPanelHeader() {
    return gettextCatalog.getString('Studies you participate in');
  }

}

export default ngModule => ngModule.component('collection', component)
