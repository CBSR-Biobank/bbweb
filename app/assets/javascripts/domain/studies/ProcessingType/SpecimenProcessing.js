/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function SpecimenProcessingFactory($log,
                                   biobankApi,
                                   InputSpecimenProcessing,
                                   OutputSpecimenProcessing,
                                   DomainEntity) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = {
    'id': 'SpecimenProcessing',
    'type': 'object',
    'properties': {
      'input':  { '$ref': 'InputSpecimenProcessing' },
      'output': { '$ref': 'OutputSpecimenProcessing' }
    },
    'required': [ 'input', 'output' ]
  };

  /**
   * @classdesc Defines the input and output {@link domain.participants.Specimen Specimen} information for a
   * {@link domain.studies.ProcessingType ProcessingType}.
   *
   * @class
   * @memberOf domain.studies
   */
  class SpecimenProcessing extends DomainEntity {

    constructor(obj = {}) {
      /**
       * The information for the input specimen.
       *
       * @name domain.studies.SpecimenProcessing#input
       * @type {domain.studies.InputSpecimenProcessing}
       */

      /**
       * The information for the output specimen.
       *
       * @name domain.studies.SpecimenProcessing#output
       * @type {domain.studies.OutputSpecimenProcessing}
       */

      super(Object.assign(
        {
          input: undefined,
          output: undefined
        },
        obj
      ));

      this.input  = new InputSpecimenProcessing(obj.input);
      this.output = new OutputSpecimenProcessing(obj.output);
    }

    /**
     * @private
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /**
     * @private
     */
    static additionalSchemas() {
      return [ InputSpecimenProcessing.schema(), OutputSpecimenProcessing.schema() ]
        .concat(InputSpecimenProcessing.additionalSchemas())
        .concat(OutputSpecimenProcessing.additionalSchemas());
    }

  }

  return SpecimenProcessing;
}

export default ngModule => ngModule.factory('SpecimenProcessing', SpecimenProcessingFactory)
