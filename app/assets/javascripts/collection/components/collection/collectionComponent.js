/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 *
 * The studyCounts object has the following fields: disabled, enabled, and retired.
 */
class CollectionController {

  constructor($state,
              gettextCatalog,
              Study,
              breadcrumbService,
              resourceErrorService) {
    'ngInject'
    Object.assign(this, {
      $state,
      gettextCatalog,
      Study,
      breadcrumbService,
      resourceErrorService
    })

    this.isCollectionAllowed = false
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.collection'),
    ]

    this.Study.collectionStudies()
      .then(reply => {
        this.isCollectionAllowed = (reply.items.length > 0);
      })
      .catch(this.resourceErrorService.checkUnauthorized())
  }

  // invoked by the SelectStudy directive
  updateEnabledStudies(options) {
    return this.Study.collectionStudies(options);
  }

  studySelected(study) {
    this.$state.go('home.collection.study', { studySlug: study.slug })
  }

}

const component = {
  template: require('./collection.html'),
  controller: CollectionController,
  controllerAs: 'vm'
}

export default ngModule => ngModule.component('collection', component)
