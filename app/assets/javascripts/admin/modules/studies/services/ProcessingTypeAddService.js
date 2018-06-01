/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 * @memberOf
 */
class ProcessingTypeAddService {

  constructor($q,
              ProcessingType,
              biobankApi) {
    'ngInject';
    Object.assign(this,
                  {
                    $q,
                    ProcessingType,
                    biobankApi
                  });
  }

  init() {
    this.processingType = new this.ProcessingType();
  }

  // used in case user reloads page
  initIfRequired() {
    if (this.processingType === undefined) {
      this.init();
    }
  }

  isValid() {
    return (this.processingType && this.processingType.name !== undefined);
  }

  getCollectedSpecimenDefinitions(study) {
    const deferred = this.$q.defer()
    if (this.eventTypes === undefined) {
      this.biobankApi.get(this.biobankApi.url('/studies/cetypes/spcdef', study.slug))
        .then((reply) => {
          this.eventTypes = reply;
          deferred.resolve(this.eventTypes);
        })
    } else {
      deferred.resolve(this.eventTypes);
    }
    return deferred.promise;
  }

}

export default ngModule => ngModule.service('ProcessingTypeAdd', ProcessingTypeAddService)
