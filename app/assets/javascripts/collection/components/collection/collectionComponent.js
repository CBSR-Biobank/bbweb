/**
 * AngularJS Component for {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace collection.components.collection
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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

/**
 * An AngularJS component that displays the main page for {@link domain.participants.Specimen Specimen}
 * collection.
 *
 * @memberOf collection.components.collection
 */
const collectionComponent = {
  template: require('./collection.html'),
  controller: CollectionController,
  controllerAs: 'vm'
};

export default ngModule => ngModule.component('collection', collectionComponent)
