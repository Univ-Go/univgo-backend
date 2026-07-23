import Image from 'next/image';

interface LogoProps {
  imageSource : string
  alt : string
  width : number
  height : number
  styles : string
}

const Logo = ({imageSource, alt, width, height, styles} : LogoProps) => {
  return (
    <>
        <Image
            src={imageSource}
            alt= {alt}  //
            width={width}    //{100}
            height={height}    //{100}
            className={styles}   //'mb-6'
        />
    </>
  )
}

export default Logo