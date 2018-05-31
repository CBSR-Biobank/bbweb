/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionSpecimenDefinitionAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

const returnState = 'home.admin.studies.study.collection.ceventType';

/*
 * Controller for this component.
 */
/* @ngInject */
class CollectionSpecimenDefinitionAddController {

  constructor($state,
              gettextCatalog,
              domainNotificationService,
              notificationsService,
              AnatomicalSourceType,
              PreservationType,
              PreservationTemperature,
              SpecimenType,
              breadcrumbService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    domainNotificationService,
                    notificationsService,
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperature,
                    SpecimenType,
                    breadcrumbService
                  });
  }

  $onInit() {
    const studySlug = this.study.slug,
          slug = this.collectionEventType.slug
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => this.study.name + ': ' + this.collectionEventType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.specimenDefinitionView',
        () => this.gettextCatalog.getString('Add collection specimen'))
    ];

    this.anatomicalSourceTypes = Object.values(this.AnatomicalSourceType);
    this.preservTypes          = Object.values(this.PreservationType);
    this.preservTempTypes      = Object.values(this.PreservationTemperature);
    this.specimenTypes         = Object.values(this.SpecimenType);
  }

  submit(specimenDefinition) {
    this.collectionEventType.addSpecimenDefinition(specimenDefinition)
      .then(() => {
        this.notificationsService.submitSuccess();
        this.$state.go(returnState, {}, { reload: true });
      })
      .catch((error) =>
             this.domainNotificationService.updateErrorModal(error, this.gettextCatalog.getString('study')));
  }

  cancel() {
    this.$state.go(returnState);
  }
}

/**
 * An AngularJS component that allows the user to add a {@link domain.studies.CollectionSpecimenDefinition
 * CollectionSpecimenDefinition} to a {@link domain.studies.CollectionEventType CollectionEventType}.
 *
 * @memberOf admin.studies.components.collectionSpecimenDefinitionAdd
 *
 * @param {domain.studies.Study} study - the study the *Collection Event Type* belongs to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the collection event type the
 * *Specimen Description* should be added to.
*/
const collectionSpecimenDefinitionAddComponent = {
  template: require('./collectionSpecimenDefinitionAdd.html'),
  controller: CollectionSpecimenDefinitionAddController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDefinitionAdd', collectionSpecimenDefinitionAddComponent)
