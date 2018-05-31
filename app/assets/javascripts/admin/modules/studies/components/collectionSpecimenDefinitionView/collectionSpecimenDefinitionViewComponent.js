class CollectionSpecimenDefinitionViewController {

  constructor($state,
              gettextCatalog,
              modalInput,
              notificationsService,
              CollectionEventType,
              CollectionSpecimenDefinition,
              AnatomicalSourceType,
              PreservationType,
              PreservationTemperature,
              SpecimenType,
              domainNotificationService,
              breadcrumbService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    modalInput,
                    notificationsService,
                    CollectionEventType,
                    CollectionSpecimenDefinition,
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperature,
                    SpecimenType,
                    domainNotificationService,
                    breadcrumbService
                  });
  }

  $onInit() {
    const studySlug = this.study.slug;
    const slug = this.collectionEventType.slug;

    this.allowEdit = this.study.isDisabled();

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection({ studySlug: "${studySlug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => this.collectionEventType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.specimenDefinitionView',
        () => this.specimenDefinition.name)
    ];

    this.returnState = {
      name: 'home.admin.studies.study.collection.ceventType',
      param: { ceventTypeId: this.collectionEventType.id }
    };

    // reload the collection event type in case changes were made to it
    this.CollectionEventType.get(studySlug, slug)
      .then(ceventType => {
        this.collectionEventType = ceventType;
      });
  }

  updateCollectionEventType() {
    return this.collectionEventType.updateSpecimenDefinition(this.specimenDefinition)
      .then((collectionEventType) => {
        this.collectionEventType = collectionEventType;
      })
      .then(() => this.notificationsService.success(
        this.gettextCatalog.getString('Specimen changed successfully.'),
        this.gettextCatalog.getString('Change successful'),
        1500))
      .catch(error => {
        this.notificationsService.updateError(error);
      });
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Specimen name'),
                         this.gettextCatalog.getString('Name'),
                         this.specimenDefinition.name,
                         { required: true, minLength: 2 }).result
      .then((name) => {
        this.specimenDefinition.name = name;
        return this.updateCollectionEventType();
      });
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Specimen description'),
                        this.gettextCatalog.getString('Description'),
                        this.specimenDefinition.description).result
      .then((description) => {
        this.specimenDefinition.description = description;
        return this.updateCollectionEventType();
      });
  }

  editAnatomicalSource() {
    this.modalInput.select(this.gettextCatalog.getString('Specimen anatomical source'),
                      this.gettextCatalog.getString('Anatomical source'),
                      this.specimenDefinition.anatomicalSourceType,
                      {
                        required: true,
                        selectOptions: Object.values(this.AnatomicalSourceType)
                      }).result
      .then((selection) => {
        this.specimenDefinition.anatomicalSourceType = selection;
        return this.updateCollectionEventType();
      });
  }

  editPreservationType() {
    this.modalInput.select(this.gettextCatalog.getString('Specimen preservation type'),
                      this.gettextCatalog.getString('Preservation type'),
                      this.specimenDefinition.preservationType,
                      {
                        required: true,
                        selectOptions: Object.values(this.PreservationType)
                      }).result
      .then((selection) => {
        this.specimenDefinition.preservationType = selection;
        return this.updateCollectionEventType();
      });
  }

  editPreservationTemperature() {
    this.modalInput.select(this.gettextCatalog.getString('Specimen preservation temperature'),
                      this.gettextCatalog.getString('Preservation temperature'),
                      this.specimenDefinition.preservationTemperature,
                      {
                        required: true,
                        selectOptions: Object.values(this.PreservationTemperature)
                      }).result
      .then((selection) => {
        this.specimenDefinition.preservationTemperature = selection;
        return this.updateCollectionEventType();
      });
  }

  editSpecimenType() {
    this.modalInput.select(this.gettextCatalog.getString('Specimen - specimen type'),
                      this.gettextCatalog.getString('Sepcimen type'),
                      this.specimenDefinition.specimenType,
                      {
                        required: true,
                        selectOptions: Object.values(this.SpecimenType)
                      }).result
      .then((selection) => {
        this.specimenDefinition.specimenType = selection;
        return this.updateCollectionEventType();
      });
  }

  editUnits() {
    this.modalInput.text(this.gettextCatalog.getString('Specimen units'),
                    this.gettextCatalog.getString('Units'),
                    this.specimenDefinition.units,
                    { required: true }).result
      .then((units) => {
        this.specimenDefinition.units = units;
        return this.updateCollectionEventType();
      });
  }

  editAmount() {
    this.modalInput.positiveFloat(this.gettextCatalog.getString('Specimen amount'),
                             this.gettextCatalog.getString('Amount'),
                             this.specimenDefinition.amount,
                             { required: true, positiveFloat: true }).result
      .then((value) => {
        this.specimenDefinition.amount = value;
        return this.updateCollectionEventType();
      });
  }

  editMaxCount() {
    this.modalInput.naturalNumber(this.gettextCatalog.getString('Specimen max count'),
                             this.gettextCatalog.getString('Max count'),
                             this.specimenDefinition.maxCount,
                             { required: true, naturalNumber: true, min: 1 }).result
      .then((value) => {
        this.specimenDefinition.maxCount = value;
        return this.updateCollectionEventType();
      });
  }

  removeRequest() {
    if (!this.study.isDisabled()) {
      throw new Error('modifications not allowed');
    }

    const removePromiseFunc =
          () => this.collectionEventType.removeSpecimenDefinition(this.specimenDefinition)
          .then(() => {
            this.notificationsService.success(this.gettextCatalog.getString('Specimen removed'));
            this.$state.go(this.returnState.name, this.returnState.param, { reload: true });
          });

    return this.domainNotificationService.removeEntity(
      removePromiseFunc,
      this.gettextCatalog.getString('Remove specimen'),
      this.gettextCatalog.getString('Are you sure you want to remove specimen {{name}}?',
                                    { name: this.specimenDefinition.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('Specimen {{name} cannot be removed',
                                    { name: this.specimenDefinition.name }));
  }

  back() {
    this.$state.go(this.returnState.name, this.returnState.param);
  }

}

/**
 * An AngularJS component that component that allows the user to view a {@link
 * domain.studies.CollectionSpecimenDefinition CollectionSpecimenDefinition} from a {@link
 * domain.studies.CollectionEventType CollectionEventType}.
 *
 * @memberOf admin.studies.components.collectionSpecimenDefinitionView
 *
 * @param {domain.studies.Study} study - the *Study* the *Collection Event Type* belongs to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the *Collection Event Type* the
 * *Specimen Description* should be added to.
 *
 * @param {domain.studies.CollectionSpecimenDefinition} specimenDefinition - The *Specimen Description* to
 * display.
 */
const collectionSpecimenDefinitionViewComponent = {
  template: require('./collectionSpecimenDefinitionView.html'),
  controller: CollectionSpecimenDefinitionViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<',
    specimenDefinition: '<'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDefinitionView',
                                             collectionSpecimenDefinitionViewComponent)
