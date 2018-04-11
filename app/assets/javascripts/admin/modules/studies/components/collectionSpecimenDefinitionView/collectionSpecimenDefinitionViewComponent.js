/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionSpecimenDefinitionView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function CollectionSpecimenDefinitionViewController($state,
                                                     gettextCatalog,
                                                     modalInput,
                                                     notificationsService,
                                                     CollectionEventType,
                                                     CollectionSpecimenDefinition,
                                                     AnatomicalSourceType,
                                                     PreservationType,
                                                     PreservationTemperature,
                                                     SpecimenType,
                                                     breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    const studySlug = vm.study.slug,
          slug = vm.collectionEventType.slug
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => vm.study.name + ': ' + vm.collectionEventType.name),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.specimenDefinitionView',
        () => vm.specimenDefinition.name)
    ];

    vm.returnState = {
      name: 'home.admin.studies.study.collection.ceventType',
      param: { ceventTypeId: vm.collectionEventType.id }
    };

    vm.editName                    = editName;
    vm.editDescription             = editDescription;
    vm.editAnatomicalSource        = editAnatomicalSource;
    vm.editPreservationType        = editPreservationType;
    vm.editPreservationTemperature = editPreservationTemperature;
    vm.editSpecimenType            = editSpecimenType;
    vm.editUnits                   = editUnits;
    vm.editAmount                  = editAmount;
    vm.editMaxCount                = editMaxCount;
    vm.back                        = back;

    // reload the collection event type in case changes were made to it
    CollectionEventType.get(studySlug, slug)
      .then(function (ceventType) {
        vm.collectionEventType = ceventType;
      });
  }

  function notifySuccess() {
    return notificationsService.success(gettextCatalog.getString('Annotation type changed successfully.'),
                                        gettextCatalog.getString('Change successful'),
                                        1500);
  }

  function updateCollectionEventType() {
    return vm.collectionEventType.updateSpecimenDefinition(vm.specimenDefinition)
      .then(function (collectionEventType) {
        vm.collectionEventType = collectionEventType;
      })
      .then(notifySuccess)
      .catch(notificationsService.updateError);
  }

  function editName() {
    modalInput.text(gettextCatalog.getString('Specimen spec name'),
                    gettextCatalog.getString('Name'),
                    vm.specimenDefinition.name,
                    { required: true, minLength: 2 }).result
      .then(function (name) {
        vm.specimenDefinition.name = name;
        return updateCollectionEventType();
      });
  }

  function editDescription() {
    modalInput.textArea(gettextCatalog.getString('Specimen spec description'),
                        gettextCatalog.getString('Description'),
                        vm.specimenDefinition.description).result
      .then(function (description) {
        vm.specimenDefinition.description = description;
        return updateCollectionEventType();
      });
  }

  function editAnatomicalSource() {
    modalInput.select(gettextCatalog.getString('Specimen spec anatomical source'),
                      gettextCatalog.getString('Anatomical source'),
                      vm.specimenDefinition.anatomicalSourceType,
                      {
                        required: true,
                        selectOptions: Object.values(AnatomicalSourceType)
                      }).result
      .then(function (selection) {
        vm.specimenDefinition.anatomicalSourceType = selection;
        return updateCollectionEventType();
      });
  }

  function editPreservationType() {
    modalInput.select(gettextCatalog.getString('Specimen spec preservation type'),
                      gettextCatalog.getString('Preservation type'),
                      vm.specimenDefinition.preservationType,
                      {
                        required: true,
                        selectOptions: Object.values(PreservationType)
                      }).result
      .then(function (selection) {
        vm.specimenDefinition.preservationType = selection;
        return updateCollectionEventType();
      });
  }

  function editPreservationTemperature() {
    modalInput.select(gettextCatalog.getString('Specimen spec preservation temperature'),
                      gettextCatalog.getString('Preservation temperature'),
                      vm.specimenDefinition.preservationTemperature,
                      {
                        required: true,
                        selectOptions: Object.values(PreservationTemperature)
                      }).result
      .then(function (selection) {
        vm.specimenDefinition.preservationTemperature = selection;
        return updateCollectionEventType();
      });
  }

  function editSpecimenType() {
    modalInput.select(gettextCatalog.getString('Specimen spec - specimen type'),
                      gettextCatalog.getString('Sepcimen type'),
                      vm.specimenDefinition.specimenType,
                      {
                        required: true,
                        selectOptions: Object.values(SpecimenType)
                      }).result
      .then(function (selection) {
        vm.specimenDefinition.specimenType = selection;
        return updateCollectionEventType();
      });
  }

  function editUnits() {
    modalInput.text(gettextCatalog.getString('Specimen spec units'),
                    gettextCatalog.getString('Units'),
                    vm.specimenDefinition.units,
                    { required: true }).result
      .then(function (units) {
        vm.specimenDefinition.units = units;
        return updateCollectionEventType();
      });
  }

  function editAmount() {
    modalInput.positiveFloat(gettextCatalog.getString('Specimen spec amount'),
                             gettextCatalog.getString('Amount'),
                             vm.specimenDefinition.amount,
                             { required: true, positiveFloat: true }).result
      .then(function (value) {
        vm.specimenDefinition.amount = value;
        return updateCollectionEventType();
      });
  }

  function editMaxCount() {
    modalInput.naturalNumber(gettextCatalog.getString('Specimen spec max count'),
                             gettextCatalog.getString('Max count'),
                             vm.specimenDefinition.maxCount,
                             { required: true, naturalNumber: true, min: 1 }).result
      .then(function (value) {
        vm.specimenDefinition.maxCount = value;
        return updateCollectionEventType();
      });
  }

  function back() {
    $state.go(vm.returnState.name, vm.returnState.param);
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
