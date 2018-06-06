class ProcessingTypeOutputFormController {

  constructor(AnatomicalSourceType,
              PreservationType,
              PreservationTemperature,
              SpecimenType) {
    'ngInject';
    Object.assign(this,
                  {
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperature,
                    SpecimenType
                  });
    this.anatomicalSourceTypes = Object.values(this.AnatomicalSourceType);
    this.preservTypes          = Object.values(this.PreservationType);
    this.preservTempTypes      = Object.values(this.PreservationTemperature);
    this.specimenTypes         = Object.values(this.SpecimenType);
  }

  $onInit() {
    const output = this.processingType.specimenProcessing.output;

    this.expectedChange          = output.expectedChange;
    this.count                   = output.count;
    this.containerTypeId         = output.containerTypeId;
    this.name                    = output.specimenDefinition.name;
    this.description             = output.specimenDefinition.description;
    this.units                   = output.specimenDefinition.units;
    this.anatomicalSourceType    = output.specimenDefinition.anatomicalSourceType;
    this.preservationType        = output.specimenDefinition.preservationType;
    this.preservationTemperature = output.specimenDefinition.preservationTemperature;
    this.specimenType            = output.specimenDefinition.specimenType;
  }

  assignValues() {
    const output = this.processingType.specimenProcessing.output;

    output.expectedChange                             = this.expectedChange;
    output.count                                      = this.count;
    output.containerTypeId                            = this.containerTypeId;
    output.specimenDefinition.name                    = this.name;
    output.specimenDefinition.description             = this.description;
    output.specimenDefinition.units                   = this.units;
    output.specimenDefinition.anatomicalSourceType    = this.anatomicalSourceType;
    output.specimenDefinition.preservationType        = this.preservationType;
    output.specimenDefinition.preservationTemperature = this.preservationTemperature;
    output.specimenDefinition.specimenType            = this.specimenType;
  }

  previous() {
    this.assignValues();
    if (this.onPrevious()) {
      this.onPrevious()(this.processingType);
    }
  }

  submit() {
    this.assignValues();
    if (this.onSubmit()) {
      this.onSubmit()(this.processingType);
    }
  }

  cancel() {
    if (this.onCancel()) {
      this.onCancel()();
    }
  }

}

/**
 * Configuration parameter for {@link
 * admin.studies.components.processingTypeOutputForm.processingTypeOutputFormComponent
 * processingTypeOutputFormComponent}.
 *
 * @enum {string}
 * @memberOf admin.studies.components.processingTypeOutputForm
 */
const ProcessingTypeOutputFormButtonConfig = {
  /** Submit and Cancel button are shown.*/
  TWO_BUTTON: '2-button',

  /** Previous, Next and Cancel button are shown.*/
  THREE_BUTTON: '3-button'
};

/**
 * An AngularJS component that
 *
 * @memberOf
 */
const processingTypeOutputFormComponent = {
  template: require('./processingTypeOutputForm.html'),
  controller: ProcessingTypeOutputFormController,
  controllerAs: 'vm',
  bindings: {
    processingType: '<',
    buttonConfig:   '@',
    onPrevious:     '&',
    onSubmit:       '&',
    onCancel:       '&'
  }
};

export default ngModule => {
  ngModule
    .constant('ProcessingTypeOutputFormButtonConfig', ProcessingTypeOutputFormButtonConfig)
   .component('processingTypeOutputForm', processingTypeOutputFormComponent);
}
